import json
from typing import Any, Dict

from utils.state_utils import get_age_group


def _age_rules(age: int) -> str:
    g = get_age_group(age)
    if g == "3-5":
        return (
            "- 전체 300~800자, 4~6페이지, 페이지당 1~3문장\n"
            "- 문장은 짧고 단순하게\n"
            "- 의성어·의태어 적극 사용, 반복 표현 포함\n"
            "- 사건 하나, 등장인물 1~2명\n"
            "- 갈등은 단순하고 빠르게 해결"
        )
    return (
        "- 전체 800~1200자, 6~8페이지, 페이지당 2~5문장\n"
        "- 도입·전개·위기·결말 구조\n"
        "- 대화문 포함\n"
        "- 주인공의 감정 변화가 드러남\n"
        "- 주인공의 선택이 결과를 만들어야 함"
    )


def build_story_prompt(story_state: Dict[str, Any], run_tags: Dict[str, Any]) -> str:
    age         = run_tags["targetAge"]
    story_lang  = run_tags["storyLangCode"]
    no_moral    = run_tags.get("noMoral", False)

    moral_rule = (
        "교훈 없음: 아이의 상상을 재미있게 펼치는 데 집중. 교육 메시지 금지."
        if no_moral else
        "부모의 교육 의도(parent_intent)를 주인공의 선택·행동·결과로 자연스럽게 녹인다. '교훈은~' 같은 설교 금지."
    )

    return f"""당신은 {age}세 어린이를 위한 동화 작가입니다.
아래 story_state 를 바탕으로 창의적인 동화를 작성하세요.

## 기본 설정
- 동화 언어: {story_lang}
- 대상 나이: {age}세 ({run_tags["ageGroup"]})
- 창의성: {run_tags["creativityLevel"]}

## 핵심 규칙
1. story_state 의 답변을 그대로 나열하지 않고, 장면·사건·감정으로 재창조한다.
2. 주인공 이름·특징·성격을 처음부터 끝까지 일관되게 유지한다.
3. 각 페이지는 한 가지 장면을 중심으로, 삽화로 그릴 수 있어야 한다.
4. {moral_rule}
5. contentForeign 은 이 단계에서 null 로 둔다.
6. imagePrompt / imageUrl / question / answer 필드는 출력하지 않는다.

## 나이별 규칙
{_age_rules(age)}

## story_state (동화 재료)
{json.dumps(story_state, ensure_ascii=False)}

## 출력 형식 (JSON만, 마크다운 없이)
{{
  "title": "동화 제목",
  "targetLangCode": "{story_lang}",
  "targetAge": {age},
  "pageCount": 0,
  "contents": [
    {{
      "sequence": 1,
      "contentKo": "동화 본문 (한국어)",
      "contentForeign": null,
      "sceneSummary": "이 장면을 한 문장으로 요약 (이미지 생성용)"
    }}
  ]
}}"""


def build_validation_prompt(
    story_state: Dict[str, Any],
    story_json: Dict[str, Any],
    run_tags: Dict[str, Any],
) -> str:
    return f"""당신은 어린이 동화 품질 검수자입니다.
아래 동화가 조건을 만족하는지 확인하고, 문제가 있으면 수정해 반환하세요.

## 검수 기준
1. 사용자 답변을 단순 나열하지 않고 이야기로 재창조했는가?
2. 캐릭터 이름·특징·성격이 처음부터 끝까지 일관되는가?
3. 각 페이지가 하나의 장면을 중심으로, 삽화로 그릴 수 있는가?
4. 교훈을 직접 설명하지 않고 행동·결과로 보여주는가?
5. 나이별 분량·문체 규칙을 만족하는가?
6. question / answer 필드가 없는가?

## 금지 표현
"교훈은", "배웠어요", "중요해요", "~해야 해요", "착한 아이가 되었어요"

## story_state
{json.dumps(story_state, ensure_ascii=False)}

## 검수할 동화
{json.dumps(story_json, ensure_ascii=False)}

## 출력 형식 (JSON만, 마크다운 없이)
- revisedStory.contents 에는 sequence / contentKo / contentForeign / sceneSummary 만 포함
- imagePrompt / imageUrl / imageAvoid 는 포함하지 않음
{{
  "passed": true,
  "issues": [],
  "revisedStory": {{
    "title": "",
    "targetLangCode": "",
    "targetAge": {run_tags["targetAge"]},
    "pageCount": 0,
    "contents": [
      {{"sequence": 1, "contentKo": "", "contentForeign": null, "sceneSummary": ""}}
    ]
  }}
}}"""
