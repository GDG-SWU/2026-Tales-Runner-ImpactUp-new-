import json
from typing import Any, Dict

from prompts.lang_map import lang_name


def build_title_prompt(
    story_state: Dict[str, Any],
    story_json: Dict[str, Any],
    run_tags: Dict[str, Any],
) -> str:
    age          = run_tags["targetAge"]
    story_lang   = run_tags["storyLangCode"]
    parent_code  = run_tags["parentLangCode"]
    parent_lang  = lang_name(parent_code)

    return f"""당신은 어린이 동화 편집자입니다.
완성된 동화를 읽고 책 제목 후보 3개를 만든 뒤, 가장 좋은 것 하나를 선정하세요.

## 기본 정보
- 대상 나이: {age}세 ({run_tags["ageGroup"]})
- 동화 언어: {story_lang}
- 부모 언어: {parent_lang} ({parent_code})

## 제목 규칙
- 동화의 분위기·주인공·핵심 사건을 함축
- 어린이가 듣기 좋고 기억하기 쉽게
- 최대 15자 (영어 6단어 이내)
- 교훈을 직접 드러내지 않음
- 주인공 이름 포함 가능

## 동화 내용
{json.dumps(story_json, ensure_ascii=False)}

## 출력 형식 (JSON만, 마크다운 없이)
- titleKo : 동화 원문 언어 ({story_lang}) 제목
- titleForeign : 부모 언어 ({parent_lang}) 번역 제목
{{
  "candidates": [
    {{"titleKo": "", "titleForeign": "", "reason": "선택 이유 한 줄"}},
    {{"titleKo": "", "titleForeign": "", "reason": "선택 이유 한 줄"}},
    {{"titleKo": "", "titleForeign": "", "reason": "선택 이유 한 줄"}}
  ],
  "selectedTitle": {{"titleKo": "", "titleForeign": "", "reason": ""}}
}}"""
