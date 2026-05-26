import json
from typing import Any, Dict, Optional

from prompts.lang_map import lang_name


def build_question_prompt(
    question_meta: Dict[str, Any],
    story_state: Dict[str, Any],
    run_tags: Dict[str, Any],
    last_exchange: Optional[Dict[str, str]] = None,
) -> str:
    """
    인터뷰 질문 하나를 생성하는 프롬프트.
    - questionKo  : 한국어 질문
    - questionForeign : 부모 언어 질문 (부모 언어코드가 ko이면 동일 값 복사)
    """
    parent_code  = run_tags["parentLangCode"]
    parent_lang  = lang_name(parent_code)
    is_parent_q  = question_meta.get("answeredBy") == "parent"
    age          = run_tags["targetAge"]
    age_group    = run_tags["ageGroup"]

    # ── 말투 ────────────────────────────────────────────────────────────────
    speech = "존댓말 (예: ~세요?, ~할 건가요?)" if is_parent_q else "반말 (예: ~야?, ~할까?, ~봐!)"

    # ── 부모 질문 전용 힌트 ─────────────────────────────────────────────────
    if is_parent_q:
        char_name = story_state.get("character", {}).get("name", "주인공")
        parent_hint = f"""
## 부모님께 드리는 마지막 질문 — 교훈·가치관 수집
이 질문은 인터뷰의 마지막으로, 부모님이 이 동화를 통해 아이에게 전하고 싶은 교훈이나 가치관을 수집합니다.

작성 지침:
- "{char_name}" 이름을 활용해 개인화된 질문을 만드세요.
- "교훈이 뭔가요?" 같은 딱딱한 표현 대신 자연스럽게 유도하세요.
  예: "{char_name}의 이야기를 통해 아이가 어떤 마음을 가졌으면 좋겠으세요?"
  예: "이 동화를 읽은 후 아이가 느꼈으면 하는 것이 있으신가요?"
- 용기, 배려, 나눔, 포기하지 않기, 친구와 협력하기, 자신감 등 구체적 가치를 떠올릴 수 있게 유도하세요.
- 답변은 동화 속 주인공의 선택·행동에 자연스럽게 반영됩니다 (직접 설교 형식 금지).
"""
    else:
        parent_hint = ""

    # ── 외국어 번역 지시 ────────────────────────────────────────────────────
    if parent_code.lower() == "ko":
        foreign_rule = "questionForeign = questionKo 와 동일한 값을 넣는다."
    else:
        foreign_rule = (
            f"questionForeign 은 반드시 {parent_lang} ({parent_code}) 로만 작성한다.\n"
            f"한국어·영어를 단 한 글자도 섞지 않는다.\n"
            f"questionKo 를 번역한 자연스러운 {parent_lang} 문장을 쓴다."
        )

    # ── 직전 대화 ───────────────────────────────────────────────────────────
    prev = ""
    if last_exchange and last_exchange.get("question") and last_exchange.get("answer"):
        prev = (
            f"이전 질문: {last_exchange['question']}\n"
            f"이전 답변: {last_exchange['answer']}\n"
            f"→ 위 답변의 단어/이미지를 자연스럽게 이어받아 다음 질문을 만든다."
        )

    state_summary = json.dumps(story_state, ensure_ascii=False)

    return f"""당신은 어린이 동화 인터뷰 진행자입니다.
아이 또는 부모에게 동화 재료를 수집하는 질문을 하나 만드세요.
{parent_hint}
## 기본 정보
- 대상 나이: {age}세 ({age_group})
- 이번 수집 대상: {question_meta["targetSlot"]}
- 질문 단계: {question_meta["id"]}
- 답변 주체: {question_meta["answeredBy"]}
- 말투: {speech}
- 부모 언어: {parent_lang} ({parent_code})

## 질문 작성 규칙
1. 한 가지 정보만 묻는다.
2. 직접적으로 "배경이 뭐야?" 대신 아이가 상상할 수 있게 묻는다.
3. 따뜻하고 짧게 쓴다 (2문장 이내).
4. 이미 수집된 슬롯은 다시 묻지 않는다.
5. 이전 답변의 핵심 단어를 자연스럽게 활용한다.

## 외국어(questionForeign) 규칙
{foreign_rule}

{f"## 이전 대화{chr(10)}{prev}" if prev else ""}

## 현재 수집된 정보
{state_summary}

## 출력 형식 (JSON만, 마크다운 없이)
{{
  "questionId": "{question_meta["id"]}",
  "questionType": "{question_meta["type"]}",
  "questionGroup": "{question_meta["group"]}",
  "targetSlot": "{question_meta["targetSlot"]}",
  "questionKo": "한국어 질문 문장",
  "questionForeign": "{parent_lang} 번역 질문 문장",
  "answeredBy": "{question_meta["answeredBy"]}"
}}"""


def build_refine_prompt(
    question: Dict[str, Any],
    answer: str,
    story_state: Dict[str, Any],
) -> str:
    """사용자 답변을 story_state 슬롯에 맞게 정제한다."""
    return f"""당신은 어린이 동화 인터뷰 데이터 정리자입니다.
사용자 답변을 분석해 story_state 의 해당 슬롯에 저장할 값을 추출하세요.

## 규칙
- 사용자가 말한 내용만 저장한다 (추측 금지).
- 사용자의 표현을 최대한 보존한다.
- 기존 story_state 와 충돌하면 기존 값을 유지하고 conflictNotes 에 기록한다.
- 저장 대상 슬롯: {question["targetSlot"]}

## 질문
{json.dumps(question, ensure_ascii=False)}

## 현재 story_state
{json.dumps(story_state, ensure_ascii=False)}

## 사용자 답변
{answer}

## 출력 형식 (JSON만, 마크다운 없이)
{{
  "updatedStoryState": {{}},
  "refinedValue": "정제된 핵심 값",
  "newlyFilledFields": [],
  "conflictNotes": []
}}"""


def build_translation_repair_prompt(
    question_ko: str,
    question_meta: Dict[str, Any],
    run_tags: Dict[str, Any],
) -> str:
    """questionForeign 이 비거나 한국어로 나온 경우 단일 질문 재번역."""
    parent_code = run_tags["parentLangCode"]
    parent_lang = lang_name(parent_code)

    if parent_code.lower() == "ko":
        rule = "questionForeign = questionKo 와 동일한 값을 반환한다."
    else:
        rule = f"반드시 {parent_lang} ({parent_code}) 로만 번역한다. 한국어·영어 혼용 금지."

    return f"""다음 한국어 질문을 {parent_lang} ({parent_code}) 로 번역하세요.

규칙: {rule}
질문 메타: {json.dumps(question_meta, ensure_ascii=False)}
한국어 원문: {question_ko}

출력 형식 (JSON만):
{{"questionForeign": "번역된 질문"}}"""
