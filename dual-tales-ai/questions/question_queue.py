from typing import Any, Dict, List

from utils.state_utils import get_age_group

# ── 아이 질문 풀 (11개 고정) ────────────────────────────────────────────────
# 순서 = 동화 구성 흐름: 캐릭터 → 배경 → 문화요소 → 사건 → 갈등 → 결말
# 11번 질문(resolution.change)에서 아이의 결말을 반드시 수집한 뒤,
# 12번 질문(부모 교훈, 고정)으로 인터뷰를 마친다.
_CHILD_QUESTIONS: List[Dict[str, Any]] = [
    # Q1 — 캐릭터 이름·정체
    {"id": "character.identity.main",          "type": "main",     "group": "character.identity",
     "targetSlot": "character.name",                "answeredBy": "child"},
    # Q2 — 캐릭터 독특한 특징
    {"id": "character.identity.followup_1",    "type": "followup", "group": "character.identity",
     "targetSlot": "character.unique_trait",         "answeredBy": "child"},
    # Q3 — 좋아하는 것
    {"id": "character.emotion_seed.main",      "type": "main",     "group": "character.emotion",
     "targetSlot": "character.likes",                "answeredBy": "child"},
    # Q4 — 두려움·약점
    {"id": "character.emotion_seed.followup_1","type": "followup", "group": "character.emotion",
     "targetSlot": "character.fear_or_weakness",     "answeredBy": "child"},
    # Q5 — 배경 장소
    {"id": "setting.sensory_place.main",       "type": "main",     "group": "setting.place",
     "targetSlot": "setting.place",                  "answeredBy": "child"},
    # Q6 — 배경 감각적 묘사
    {"id": "setting.sensory_place.followup_1", "type": "followup", "group": "setting.place",
     "targetSlot": "setting.sensory_detail",         "answeredBy": "child"},
    # Q7 — 문화 요소 (물건·장소)
    {"id": "culture.element.main",             "type": "main",     "group": "culture.element",
     "targetSlot": "culture_element.object_or_place","answeredBy": "child"},
    # Q8 — 사건 발생
    {"id": "plot.incident.main",               "type": "main",     "group": "plot.incident",
     "targetSlot": "plot.incident",                  "answeredBy": "child"},
    # Q9 — 첫 반응·감정
    {"id": "plot.incident.followup_1",         "type": "followup", "group": "plot.incident",
     "targetSlot": "plot.first_reaction",            "answeredBy": "child"},
    # Q10 — 갈등·장애물
    {"id": "conflict.choice.main",             "type": "main",     "group": "conflict.choice",
     "targetSlot": "conflict.obstacle",              "answeredBy": "child"},
    # Q11 — 결말: 주인공의 선택과 변화 (반드시 수집)
    {"id": "resolution.ending.main",           "type": "main",     "group": "resolution.ending",
     "targetSlot": "resolution.change",              "answeredBy": "child"},
]

# ── 부모 질문 (Q12, 고정) ────────────────────────────────────────────────────
# 항상 마지막에 위치. 교훈·가치관을 묻는 고정 질문.
_PARENT_QUESTION: Dict[str, Any] = {
    "id":          "parent.story_intent.main",
    "type":        "main",
    "group":       "parent.story_intent",
    "targetSlot":  "parent_intent.value",
    "answeredBy":  "parent",
}

# ── 3~5세: 아이 질문 중 생략 가능한 ID (복잡한 꼬리질문 제외) ──────────────
_SKIP_FOR_YOUNG = {
    "character.emotion_seed.followup_1",  # Q4 두려움 (어린 아이엔 어려울 수 있음)
    "setting.sensory_place.followup_1",   # Q6 감각 묘사
}


def get_question_queue(
    target_age: int,
    no_moral: bool = False,
) -> List[Dict[str, Any]]:
    """
    질문 목록 반환.

    구조 (고정):
      - 아이 질문 Q1~Q11 (3~5세는 _SKIP_FOR_YOUNG 제외 → 최대 9개)
      - 부모 교훈 질문 Q12 (no_moral=True 이면 생략)

    Q11(resolution.change)은 결말 수집용으로 절대 생략하지 않는다.
    """
    age_group  = get_age_group(target_age)
    child_qs   = list(_CHILD_QUESTIONS)

    if age_group == "3-5":
        child_qs = [q for q in child_qs if q["id"] not in _SKIP_FOR_YOUNG]

    if no_moral:
        return child_qs

    return child_qs + [_PARENT_QUESTION]
