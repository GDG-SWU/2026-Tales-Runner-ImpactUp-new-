"""
══════════════════════════════════════════════════════════
Story Generator v2 — FastAPI 서버
══════════════════════════════════════════════════════════

엔드포인트:
  POST /v1/ai/generate   → 질문(QUESTION) / 동화(STORY) 통합
  GET  /health           → 서버 상태 확인

이미지 저장 방식:
  - Imagen 생성 → 로컬 IMAGE_DIR에 JPEG 저장 (UUID 파일명, 덮어쓰기 없음)
  - FastAPI StaticFiles로 /images/* URL 서빙
  - 응답 JSON의 image_url 예시: https://your-server.com/images/cover_a3f2b1c4.jpg
══════════════════════════════════════════════════════════
"""

import os
import sys
from dotenv import load_dotenv

load_dotenv()

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
from typing import List, Optional

from config.settings import OUTPUT_DIR, IMAGE_DIR, FAST_MODEL, get_creativity_config
from prompts.question_prompts import (
    build_question_prompt,
    build_refine_prompt,
    build_translation_repair_prompt,
)
from prompts.story_prompts import build_story_prompt, build_validation_prompt
from prompts.title_prompts import build_title_prompt
from prompts.translation_prompts import build_translation_prompt
from prompts.image_prompts import (
    build_page_image_prompt,
    build_cover_image_prompt,
    fallback_page_prompt,
    fallback_cover_prompt,
)
from questions.question_queue import get_question_queue
from services.gemini_service import GeminiService
from services.image_service import generate_image
from state.story_state import empty_story_state, make_run_tags
from utils.json_utils import remove_qa_fields
from utils.state_utils import deep_merge, get_nested, set_nested

# ────────────────────────────────────────────────────────────────────────────
# FastAPI 앱
# ────────────────────────────────────────────────────────────────────────────

app = FastAPI(
    title="Story Generator AI API",
    description="어린이 동화 생성 AI 서버",
    version="2.0.0",
)

IMAGE_DIR.mkdir(parents=True, exist_ok=True)
app.mount("/images", StaticFiles(directory=str(IMAGE_DIR)), name="images")


def _image_url(filename: str) -> str:
    """파일명 → 외부 접근 가능한 URL. BASE_URL 환경변수로 도메인 설정."""
    base = os.environ.get("BASE_URL", "").rstrip("/")
    return f"{base}/images/{filename}" if base else f"/images/{filename}"


# ────────────────────────────────────────────────────────────────────────────
# 요청/응답 스키마
# ────────────────────────────────────────────────────────────────────────────

class AiRequest(BaseModel):
    """
    request_type     : "QUESTION" | "STORY"
    target_lang_code : 부모 언어 코드 (예: "EN", "KO")
    story_lang_code  : 동화 원문 언어 코드 (기본 "KO")
    target_age       : 아이 나이 (3~8세)
    history          : 누적 히스토리 "Q1: ...\\nA: ..."
    user_answer      : 방금 입력한 답변 (첫 질문이면 null)
    story_state      : 이전 QUESTION 응답의 story_state 그대로 전달
    no_moral         : True이면 부모 교훈 질문 생략 & 동화에 교훈 미포함
    """
    request_type: str
    target_lang_code: str
    story_lang_code: str = "KO"
    target_age: int
    history: str = ""
    user_answer: Optional[str] = None
    story_state: Optional[dict] = None
    no_moral: bool = False


class QuestionResponse(BaseModel):
    question_ko: str
    question_foreign: str
    is_final: bool
    story_state: dict


class StoryPage(BaseModel):
    sequence: int
    content_ko: str
    content_foreign: str
    image_url: Optional[str] = ""  # https://your-server.com/images/page_1_a3f2b1c4.jpg


class StoryResponse(BaseModel):
    title: str
    cover_image_url: Optional[str] = ""  # https://your-server.com/images/cover_a3f2b1c4.jpg
    target_lang_code: str
    target_age: int
    page_count: int
    pages: List[StoryPage]


# ────────────────────────────────────────────────────────────────────────────
# 내부 헬퍼
# ────────────────────────────────────────────────────────────────────────────

def _has_korean(text: str) -> bool:
    return any("가" <= ch <= "힣" for ch in (text or ""))


def _pick_representative_page_image(final_story: dict, story_state: dict) -> str:
    """
    표지 생성 실패 시 본문 페이지 이미지 중 가장 대표적인 URL 반환.
    선택 기준: 키워드 매칭 점수 + 중·후반부 위치(50~80%) 가중치.
    """
    contents = final_story.get("contents", [])
    pages_with_img = [p for p in contents if p.get("imageUrl")]
    if not pages_with_img:
        return ""

    plot = story_state.get("plot", {})
    keywords_raw = " ".join(filter(None, [
        plot.get("incident", ""),
        plot.get("resolution", ""),
        story_state.get("character", {}).get("unique_trait", ""),
    ])).lower()
    keywords = [w for w in keywords_raw.split() if len(w) > 1]

    def _keyword_score(page: dict) -> int:
        text = (page.get("contentKo", "") + " " + page.get("sceneSummary", "")).lower()
        return sum(1 for kw in keywords if kw in text)

    total = len(contents)

    def _position_score(page: dict) -> float:
        ratio = page.get("sequence", 1) / max(total, 1)
        if 0.5 <= ratio <= 0.8:
            return 1.0
        elif ratio < 0.5:
            return ratio / 0.5
        else:
            return (1.0 - ratio) / 0.2

    best = max(pages_with_img, key=lambda p: (_keyword_score(p) * 2 + _position_score(p)))
    print(f"[표지 폴백] Page {best['sequence']} 이미지를 표지로 대체합니다.")
    return best["imageUrl"]


def _is_valid_question_foreign(q: dict, lang_code: str) -> bool:
    ko      = (q.get("questionKo") or "").strip()
    foreign = (q.get("questionForeign") or "").strip()
    if not ko or not foreign:
        return False
    if lang_code.lower() == "ko":
        return True
    return not (foreign == ko or _has_korean(foreign))


def _count_qa_pairs(history: str) -> int:
    if not history.strip():
        return 0
    return sum(
        1 for line in history.strip().split("\n")
        if line.strip().lower().startswith(("a:", "a :"))
    )


def _resolve_story_state(req_story_state: Optional[dict]) -> dict:
    return req_story_state if req_story_state else empty_story_state()


# ────────────────────────────────────────────────────────────────────────────
# 엔드포인트
# ────────────────────────────────────────────────────────────────────────────

@app.get("/health")
def health_check():
    return {"status": "ok", "message": "Story Generator AI 서버 정상 동작 중"}


@app.post("/v1/ai/generate")
async def generate(req: AiRequest):
    if req.request_type == "QUESTION":
        return await _handle_question(req)
    elif req.request_type == "STORY":
        return await _handle_story(req)
    else:
        raise HTTPException(
            status_code=400,
            detail=f"request_type은 'QUESTION' 또는 'STORY'여야 합니다. 받은 값: {req.request_type}"
        )


# ────────────────────────────────────────────────────────────────────────────
# QUESTION 처리
# ────────────────────────────────────────────────────────────────────────────

async def _handle_question(req: AiRequest) -> QuestionResponse:
    try:
        svc            = GeminiService()
        cfg            = get_creativity_config("medium")
        lang_code      = req.target_lang_code.lower()
        story_lang     = req.story_lang_code.lower()
        run_tags       = make_run_tags(
            target_age=req.target_age,
            parent_lang_code=lang_code,
            story_lang_code=story_lang,
            no_moral=req.no_moral,
        )
        question_queue = get_question_queue(req.target_age, no_moral=req.no_moral)
        current_idx    = _count_qa_pairs(req.history)

        if current_idx >= len(question_queue):
            raise HTTPException(status_code=400, detail="인터뷰가 이미 완료됐습니다.")

        meta        = question_queue[current_idx]
        story_state = _resolve_story_state(req.story_state)

        if req.user_answer and current_idx > 0:
            prev_meta = question_queue[current_idx - 1]
            prev_q    = {"questionKo": "", "questionForeign": "", "targetSlot": prev_meta["targetSlot"]}
            refined   = svc.call_json(
                build_refine_prompt(question=prev_q, answer=req.user_answer, story_state=story_state),
                f"답변정제 {current_idx}", temperature=cfg["refine"], model=FAST_MODEL,
            )
            story_state = deep_merge(story_state, refined.get("updatedStoryState", {}))
            if not get_nested(story_state, prev_meta["targetSlot"]) and refined.get("refinedValue"):
                set_nested(story_state, prev_meta["targetSlot"], refined["refinedValue"])

        exchange = {"question": "", "answer": req.user_answer} if req.user_answer else None

        question = svc.call_json(
            build_question_prompt(
                question_meta=meta,
                story_state=story_state,
                run_tags=run_tags,
                last_exchange=exchange,
            ),
            f"질문생성 {current_idx+1}/{len(question_queue)}",
            temperature=cfg["question"], model=FAST_MODEL,
        )

        if not _is_valid_question_foreign(question, lang_code):
            fixed = svc.call_json(
                build_translation_repair_prompt(question.get("questionKo", ""), meta, run_tags),
                f"번역보정 {current_idx+1}",
                temperature=cfg["translation"], model=FAST_MODEL,
            )
            if fixed.get("questionForeign"):
                question["questionForeign"] = fixed["questionForeign"]

        return QuestionResponse(
            question_ko=question.get("questionKo", ""),
            question_foreign=question.get("questionForeign", ""),
            is_final=(current_idx == len(question_queue) - 1),
            story_state=story_state,
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"질문 생성 실패: {str(e)}")


# ────────────────────────────────────────────────────────────────────────────
# STORY 처리
# ────────────────────────────────────────────────────────────────────────────

async def _handle_story(req: AiRequest) -> StoryResponse:
    try:
        svc        = GeminiService()
        cfg        = get_creativity_config("medium")
        lang_code  = req.target_lang_code.lower()
        story_lang = req.story_lang_code.lower()
        run_tags   = make_run_tags(
            target_age=req.target_age,
            parent_lang_code=lang_code,
            story_lang_code=story_lang,
            generate_images=True,
            no_moral=req.no_moral,
        )

        OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
        IMAGE_DIR.mkdir(parents=True, exist_ok=True)

        story_state = _resolve_story_state(req.story_state)

        # STEP 3: 동화 초안
        story_json = svc.call_json(
            build_story_prompt(story_state=story_state, run_tags=run_tags),
            "동화생성", temperature=cfg["story"],
        )
        story_json = remove_qa_fields(story_json)

        # STEP 3.5: 제목
        title_result  = svc.call_json(
            build_title_prompt(story_state=story_state, story_json=story_json, run_tags=run_tags),
            "제목생성", temperature=cfg["title"],
        )
        selected      = title_result.get("selectedTitle", {})
        title_ko      = selected.get("titleKo") or story_json.get("title", "")
        title_foreign = selected.get("titleForeign") or story_json.get("title", "")
        story_json["title"]        = title_ko
        story_json["titleForeign"] = title_foreign

        # STEP 4: 품질 검수
        validation  = svc.call_json(
            build_validation_prompt(story_state=story_state, story_json=story_json, run_tags=run_tags),
            "품질검수", temperature=cfg["validation"],
        )
        revised     = validation.get("revisedStory")
        final_story = revised if (revised and revised.get("contents")) else story_json
        final_story.setdefault("title", title_ko)
        final_story.setdefault("titleForeign", title_foreign)
        final_story = remove_qa_fields(final_story)

        # STEP 5: 번역
        translation = svc.call_json(
            build_translation_prompt(story_json=final_story, run_tags=run_tags),
            "번역", temperature=cfg["translation"], model=FAST_MODEL,
        )
        t_foreign = (translation.get("titleForeign") or "").strip()
        final_story["titleForeign"] = t_foreign if t_foreign else title_foreign

        seq_to_foreign = {
            item["sequence"]: item["contentForeign"]
            for item in translation.get("contents", [])
            if "sequence" in item and "contentForeign" in item
        }
        for page in final_story.get("contents", []):
            foreign = (seq_to_foreign.get(page["sequence"]) or "").strip()
            page["contentForeign"] = foreign if foreign else page.get("contentKo", "")
        final_story = remove_qa_fields(final_story)

        # STEP 6~7: 이미지 생성
        cover_image_url = ""
        cover_generated = False

        # ── 표지 이미지 ─────────────────────────────────────────────────────
        try:
            cover_prompt_json = svc.call_json(
                build_cover_image_prompt(
                    story_state=story_state,
                    story_title=title_ko,
                    title_foreign=title_foreign,
                    run_tags=run_tags,
                ),
                "표지프롬프트", temperature=cfg["img_prompt"], model=FAST_MODEL,
            )
            cover_path = generate_image(
                gemini_service=svc,
                prompt=cover_prompt_json.get("imagePrompt") or fallback_cover_prompt(story_state),
                sequence=0, aspect_ratio="3:4", display_image=False,
                fallback_prompt=fallback_cover_prompt(story_state), max_attempts=2,
            )
            cover_image_url = _image_url(cover_path.name)
            cover_generated = True
        except Exception as e:
            print(f"[표지 이미지 생성 실패] {e} — 페이지 이미지로 대체 예정")

        # ── 본문 페이지 이미지 ───────────────────────────────────────────────
        for page in final_story.get("contents", []):
            ip_result = svc.call_json(
                build_page_image_prompt(
                    story_state=story_state, story_title=title_ko,
                    run_tags=run_tags, page=page,
                ),
                f"페이지{page['sequence']}이미지프롬프트",
                temperature=cfg["img_prompt"], model=FAST_MODEL,
            )
            try:
                page_path = generate_image(
                    gemini_service=svc,
                    prompt=ip_result.get("imagePrompt") or fallback_page_prompt(story_state, page),
                    sequence=page["sequence"], aspect_ratio="1:1", display_image=False,
                    fallback_prompt=fallback_page_prompt(story_state, page),
                )
                page["imageUrl"] = _image_url(page_path.name)
            except Exception as e:
                print(f"[Page {page['sequence']} 이미지 실패] {e}")
                page["imageUrl"] = ""

        # ── 표지 실패 시 대표 페이지 이미지로 대체 ──────────────────────────
        if not cover_generated:
            cover_image_url = _pick_representative_page_image(final_story, story_state)

        # 응답 조립
        pages = [
            StoryPage(
                sequence=p["sequence"],
                content_ko=p.get("contentKo", ""),
                content_foreign=p.get("contentForeign", ""),
                image_url=p.get("imageUrl", ""),
            )
            for p in final_story.get("contents", [])
        ]

        return StoryResponse(
            title=final_story.get("title", ""),
            cover_image_url=cover_image_url,
            target_lang_code=req.target_lang_code.upper(),
            target_age=req.target_age,
            page_count=len(pages),
            pages=pages,
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"동화 생성 실패: {str(e)}")


# ────────────────────────────────────────────────────────────────────────────
# 로컬 실행
# ────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
