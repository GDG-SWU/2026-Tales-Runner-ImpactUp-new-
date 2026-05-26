from typing import Any, Dict, List, Optional

from config.settings import OUTPUT_DIR, FAST_MODEL, get_creativity_config
from prompts.image_prompts import (
    build_page_image_prompt,
    build_cover_image_prompt,
    fallback_page_prompt,
    fallback_cover_prompt,
)
from prompts.question_prompts import (
    build_question_prompt,
    build_refine_prompt,
    build_translation_repair_prompt,
)
from prompts.story_prompts import build_story_prompt, build_validation_prompt
from prompts.title_prompts import build_title_prompt
from prompts.translation_prompts import build_translation_prompt
from prompts.lang_map import lang_name
from questions.question_queue import get_question_queue
from services.gemini_service import GeminiService
from services.image_service import generate_image
from state.story_state import empty_story_state, make_run_tags
from utils.json_utils import remove_qa_fields, save_json
from utils.state_utils import get_nested, set_nested, deep_merge


# ────────────────────────────────────────────────────────────────────────────
# 내부 헬퍼
# ────────────────────────────────────────────────────────────────────────────

def _input(prompt_text: str) -> str:
    try:
        return input(prompt_text).strip()
    except (EOFError, KeyboardInterrupt):
        return ""


def _has_korean(text: str) -> bool:
    return any("가" <= ch <= "힣" for ch in (text or ""))


def _is_valid_question_foreign(q: Dict, parent_code: str) -> bool:
    ko      = (q.get("questionKo") or "").strip()
    foreign = (q.get("questionForeign") or "").strip()
    if not ko or not foreign:
        return False
    if parent_code.lower() == "ko":
        return True
    if foreign == ko or _has_korean(foreign):
        return False
    return True


def _repair_question_foreign(
    svc: GeminiService,
    q: Dict,
    meta: Dict,
    run_tags: Dict,
    temperature: float,
) -> Dict:
    """questionForeign 누락/미번역 시 단일 재번역."""
    parent_code = run_tags["parentLangCode"]
    if _is_valid_question_foreign(q, parent_code):
        return q
    prompt   = build_translation_repair_prompt(q.get("questionKo", ""), meta, run_tags)
    repaired = svc.call_json(prompt, f"STEP 1-R 질문번역보정 {meta['id']}", temperature=temperature, model=FAST_MODEL)
    fixed    = (repaired.get("questionForeign") or "").strip()
    if fixed:
        q["questionForeign"] = fixed
    return q


def _validate_translation(translation: Dict, final_story: Dict, same_lang: bool) -> bool:
    """번역 결과 유효성 검사."""
    expected = {p["sequence"] for p in final_story.get("contents", [])}
    got      = {item.get("sequence") for item in translation.get("contents", []) if "sequence" in item}
    if expected != got:
        print(f"[STEP 5] sequence 불일치: 기대={expected}, 수신={got}")
        return False

    ko_map = {p["sequence"]: p.get("contentKo", "") for p in final_story.get("contents", [])}
    for item in translation.get("contents", []):
        seq     = item.get("sequence")
        foreign = (item.get("contentForeign") or "").strip()
        if not foreign:
            print(f"[STEP 5] Page {seq} contentForeign 비어 있음")
            return False
        if not same_lang and foreign == ko_map.get(seq, "").strip():
            print(f"[STEP 5] Page {seq} 번역 미수행 (원문과 동일)")
            return False
    return True


# ────────────────────────────────────────────────────────────────────────────
# 메인 파이프라인
# ────────────────────────────────────────────────────────────────────────────

def run_story_pipeline(
    target_age: int,
    parent_lang_code: str,
    story_lang_code: str = "ko",
    creativity_level: str = "medium",
    generate_images: bool = True,
    image_style: str = "warm watercolor children picture book illustration",
    display_images: bool = True,
    api_key: Optional[str] = None,
    skip_validation: bool = False,
    no_moral: bool = False,
    use_demo_answers: bool = False,
) -> Dict[str, Any]:
    """
    어린이 동화 생성 파이프라인 (v2).

    수정 사항:
    - 번역 프롬프트 단순화 → 번역 누락 방지
    - 표지 이미지 생성 (텍스트 오버레이 없음, 1:1 비율)
    - API 오류 지수 백오프 재시도 (GeminiService)
    - 모든 프롬프트 간결화 → 모델 인식 부하 감소
    """
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    run_tags = make_run_tags(
        target_age=target_age,
        parent_lang_code=parent_lang_code,
        story_lang_code=story_lang_code,
        creativity_level=creativity_level,
        generate_images=generate_images,
        image_style=image_style,
        no_moral=no_moral,
    )

    cfg = get_creativity_config(creativity_level)
    svc = GeminiService(api_key=api_key)

    story_state    = empty_story_state()
    question_queue = get_question_queue(target_age, no_moral=no_moral)
    interview_logs: List[Dict[str, Any]] = []
    last_exchange: Optional[Dict[str, str]] = None

    # ── DEMO 답변 (테스트용) ─────────────────────────────────────────────────
    _demo_answers = [
        "코코",        # 이름
        "반짝이는 꼬리", # 특징
        "별 보기",     # 좋아하는 것
        "혼자 있기",   # 두려움
        "꼬리를 흔들어요", # 감정 표현
        "별빛 숲",     # 배경
        "반짝반짝 빛나는 이슬",  # 감각적 묘사
        "빛나는 큰 나무", # 시각 앵커
        "반딧불이",    # 문화 요소
        "초록빛 날개", # 외양
        "함께하면 밝아져요", # 감정적 연결
        "반딧불이를 잃어버렸어요", # 사건
    ]
    _demo_idx = [0]

    def _get_answer(prompt_text: str) -> str:
        if use_demo_answers and _demo_idx[0] < len(_demo_answers):
            ans = _demo_answers[_demo_idx[0]]
            _demo_idx[0] += 1
            print(f"[DEMO] {ans}")
            return ans
        return _input(prompt_text)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 1~2 : 인터뷰 (질문 생성 + 답변 정제)
    # ════════════════════════════════════════════════════════════════════════
    print("\n" + "=" * 60)
    print("📖 어린이 동화 인터뷰를 시작합니다!")
    print("=" * 60)

    total_q = len(question_queue)
    for idx, meta in enumerate(question_queue):
        # STEP 1. 질문 생성
        q_prompt = build_question_prompt(
            question_meta=meta,
            story_state=story_state,
            run_tags=run_tags,
            last_exchange=last_exchange,
        )
        question = svc.call_json(q_prompt, f"STEP 1 질문생성 {idx+1}/{len(question_queue)}",
                                  temperature=cfg["question"], model=FAST_MODEL)

        # 번역 보정
        question = _repair_question_foreign(svc, question, meta, run_tags, cfg["translation"])

        is_parent_q = meta["answeredBy"] == "parent"
        label = "👨‍👩‍👧 부모님께 (마지막 질문)" if is_parent_q else f"🧒 아이에게 ({idx+1}/{total_q})"

        # 질문 출력
        print(f"\n[질문 {idx+1}/{total_q}] {label}")
        print(f"  {question.get('questionKo', '')}")
        if question.get("questionForeign") and question["questionForeign"] != question.get("questionKo"):
            print(f"  {question['questionForeign']}")

        # 답변 수집
        answer = _get_answer("  → 답변: ")

        # STEP 2. 답변 정제
        refine_prompt = build_refine_prompt(question=question, answer=answer, story_state=story_state)
        refined = svc.call_json(refine_prompt, f"STEP 2 답변정제 {idx+1}/{len(question_queue)}",
                                 temperature=cfg["refine"], model=FAST_MODEL)

        story_state = deep_merge(story_state, refined.get("updatedStoryState", {}))
        if not get_nested(story_state, meta["targetSlot"]) and refined.get("refinedValue"):
            set_nested(story_state, meta["targetSlot"], refined["refinedValue"])

        interview_logs.append({
            "sequence":       idx + 1,
            "questionId":     question.get("questionId"),
            "questionType":   question.get("questionType"),
            "questionGroup":  question.get("questionGroup"),
            "targetSlot":     question.get("targetSlot"),
            "questionKo":     question.get("questionKo"),
            "questionForeign":question.get("questionForeign"),
            "answeredBy":     question.get("answeredBy"),
            "answer":         answer,
            "refinedValue":   refined.get("refinedValue"),
        })
        last_exchange = {"question": question.get("questionKo", ""), "answer": answer}

        # 부모 질문(마지막)은 답변 후 인터뷰 종료
        if is_parent_q:
            print("\n  ✅ 인터뷰 완료!")
            break

    save_json(OUTPUT_DIR / "00_run_tags.json",       run_tags)
    save_json(OUTPUT_DIR / "01_interview_logs.json", {"answers": interview_logs})
    save_json(OUTPUT_DIR / "02_story_state.json",    story_state)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 3 : 동화 초안 생성
    # ════════════════════════════════════════════════════════════════════════
    print("\n[STEP 3] 동화 초안 생성 중...")
    story_prompt = build_story_prompt(story_state=story_state, run_tags=run_tags)
    story_json   = svc.call_json(story_prompt, "STEP 3 동화생성", temperature=cfg["story"])
    story_json   = remove_qa_fields(story_json)
    save_json(OUTPUT_DIR / "03_story_draft.json", story_json)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 3.5 : 책 제목 생성
    # ════════════════════════════════════════════════════════════════════════
    print("[STEP 3.5] 책 제목 생성 중...")
    title_prompt  = build_title_prompt(story_state=story_state, story_json=story_json, run_tags=run_tags)
    title_result  = svc.call_json(title_prompt, "STEP 3.5 제목생성", temperature=cfg["title"])
    selected      = title_result.get("selectedTitle", {})
    title_ko      = selected.get("titleKo") or story_json.get("title", "")
    title_foreign = selected.get("titleForeign") or story_json.get("title", "")

    story_json["title"]          = title_ko
    story_json["titleForeign"]   = title_foreign
    story_json["titleCandidates"] = title_result.get("candidates", [])
    save_json(OUTPUT_DIR / "03_5_title_result.json", title_result)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 4 : 품질 검수
    # ════════════════════════════════════════════════════════════════════════
    if skip_validation:
        final_story = story_json
    else:
        print("[STEP 4] 동화 품질 검수 중...")
        val_prompt  = build_validation_prompt(story_state=story_state, story_json=story_json, run_tags=run_tags)
        validation  = svc.call_json(val_prompt, "STEP 4 검수", temperature=cfg["validation"])
        revised     = validation.get("revisedStory")

        if revised and revised.get("contents"):
            revised.setdefault("title",          title_ko)
            revised.setdefault("titleForeign",   title_foreign)
            revised["titleCandidates"] = story_json.get("titleCandidates", [])
            # 이전 이미지 필드 유지
            orig_map = {p["sequence"]: p for p in story_json.get("contents", [])}
            for page in revised.get("contents", []):
                orig = orig_map.get(page["sequence"], {})
                for field in ("imagePrompt", "imageUrl", "imageAvoid"):
                    if not page.get(field):
                        page[field] = orig.get(field)
            final_story = revised
        else:
            final_story = story_json

    final_story = remove_qa_fields(final_story)
    save_json(OUTPUT_DIR / "04_story_validated.json", final_story)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 5 : 번역
    # ════════════════════════════════════════════════════════════════════════
    print("[STEP 5] 번역 중...")
    parent_code = run_tags["parentLangCode"]
    same_lang   = parent_code.lower() == story_lang_code.lower()

    trans_prompt = build_translation_prompt(story_json=final_story, run_tags=run_tags)
    translation  = svc.call_json(trans_prompt, "STEP 5 번역", temperature=cfg["translation"], model=FAST_MODEL)

    if not _validate_translation(translation, final_story, same_lang):
        # 번역 1회 재시도
        print("[STEP 5] 번역 결과 불완전 → 재시도...")
        translation = svc.call_json(trans_prompt, "STEP 5 번역재시도", temperature=cfg["translation"], model=FAST_MODEL)

    # titleForeign 적용
    t_foreign = (translation.get("titleForeign") or "").strip()
    final_story["titleForeign"] = t_foreign if t_foreign else title_foreign

    # contentForeign 적용
    seq_to_foreign = {
        item["sequence"]: item["contentForeign"]
        for item in translation.get("contents", [])
        if "sequence" in item and "contentForeign" in item
    }
    for page in final_story.get("contents", []):
        seq     = page["sequence"]
        foreign = (seq_to_foreign.get(seq) or "").strip()
        ko_text = page.get("contentKo", "")
        if not foreign or (not same_lang and foreign == ko_text.strip()):
            page["contentForeign"] = ko_text if same_lang else ""
        else:
            page["contentForeign"] = foreign

    final_story = remove_qa_fields(final_story)
    save_json(OUTPUT_DIR / "05_story_translated.json", final_story)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 6 : 이미지 프롬프트 생성 (generate_images=True 시에만)
    # ════════════════════════════════════════════════════════════════════════
    if generate_images:
        print("[STEP 6] 이미지 프롬프트 생성 중...")

        # 본문 페이지 프롬프트
        for page in final_story.get("contents", []):
            ip_prompt = build_page_image_prompt(
                story_state=story_state,
                story_title=final_story.get("title", ""),
                run_tags=run_tags,
                page=page,
            )
            ip_result = svc.call_json(
                ip_prompt,
                f"STEP 6 페이지{page['sequence']}이미지프롬프트",
                temperature=cfg["img_prompt"],
                model=FAST_MODEL,
            )
            page["imagePrompt"] = ip_result.get("imagePrompt")
            page["imageAvoid"]  = ip_result.get("avoid", [])

        # 표지 프롬프트
        cover_prompt_raw = build_cover_image_prompt(
            story_state=story_state,
            story_title=final_story.get("title", ""),
            title_foreign=final_story.get("titleForeign", ""),
            run_tags=run_tags,
        )
        cover_prompt_json = svc.call_json(
            cover_prompt_raw,
            "STEP 6.5 표지이미지프롬프트",
            temperature=cfg["img_prompt"],
            model=FAST_MODEL,
        )
        final_story["coverImagePrompt"] = cover_prompt_json.get("imagePrompt")
        final_story["coverImageAvoid"]  = cover_prompt_json.get("avoid", [])

    final_story = remove_qa_fields(final_story)
    save_json(OUTPUT_DIR / "06_story_with_image_prompts.json", final_story)

    # ════════════════════════════════════════════════════════════════════════
    # STEP 7 : Imagen 4 이미지 생성
    # ════════════════════════════════════════════════════════════════════════
    if generate_images:
        print("[STEP 7] 이미지 생성 중...")

        # 7-0. 표지 이미지 생성 + 제목 오버레이
        cover_img_prompt = final_story.get("coverImagePrompt")
        cover_fb         = fallback_cover_prompt(story_state)
        cover_generated  = False

        try:
            cover_path = generate_image(
                gemini_service=svc,
                prompt=cover_img_prompt or cover_fb,
                sequence=0,
                aspect_ratio="1:1",
                display_image=False,
                fallback_prompt=cover_fb,
                max_attempts=2,
            )
            final_story["coverImageUrl"] = str(cover_path)
            cover_generated = True
            if display_images:
                from services.image_service import _display_colab
                _display_colab(cover_path, caption="표지(Cover)")
        except Exception as e:
            print(f"[STEP 7] 표지 이미지 생성 실패: {e} — 페이지 이미지로 대체 예정")

        # 7-1. 본문 페이지 이미지
        for page in final_story.get("contents", []):
            img_prompt = page.get("imagePrompt")
            fb_prompt  = fallback_page_prompt(story_state, page)
            try:
                page_path = generate_image(
                    gemini_service=svc,
                    prompt=img_prompt or fb_prompt,
                    sequence=page["sequence"],
                    aspect_ratio="1:1",
                    display_image=display_images,
                    fallback_prompt=fb_prompt,
                )
                page["imageUrl"] = str(page_path)
            except Exception as e:
                print(f"[STEP 7] Page {page['sequence']} 이미지 생성 실패: {e}")
                page["imageUrl"] = ""

        # 7-2. 표지 실패 시 첫 번째 페이지 이미지로 대체
        if not cover_generated:
            pages_with_img = [p for p in final_story.get("contents", []) if p.get("imageUrl")]
            if pages_with_img:
                final_story["coverImageUrl"] = pages_with_img[0]["imageUrl"]
                print("[STEP 7] Page 1 이미지를 표지로 대체했습니다.")
            else:
                final_story["coverImageUrl"] = ""

    final_story = remove_qa_fields(final_story)
    save_json(OUTPUT_DIR / "07_final_backend_payload.json", final_story)

    print("\n" + "=" * 60)
    print(f"✅ 동화 생성 완료!")
    print(f"   제목 (원문):  {final_story.get('title')}")
    print(f"   제목 (번역):  {final_story.get('titleForeign')}")
    if final_story.get("coverImageUrl"):
        print(f"   표지 이미지:  {final_story['coverImageUrl']}")
    print(f"   총 페이지:    {final_story.get('pageCount', len(final_story.get('contents', [])))}")
    print("=" * 60)

    return final_story
