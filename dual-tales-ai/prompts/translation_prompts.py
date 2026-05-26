import json
from typing import Any, Dict

from prompts.lang_map import lang_name


def build_translation_prompt(story_json: Dict[str, Any], run_tags: Dict[str, Any]) -> str:
    """
    동화 본문 + 제목을 부모 언어로 번역.
    - 번역 실패의 핵심 원인: 프롬프트가 복잡해 모델이 '번역 생략'을 선택.
    - 해결책: 페이지 목록을 명시적으로 열거해 번역 누락을 방지.
    """
    parent_code  = run_tags["parentLangCode"]
    parent_lang  = lang_name(parent_code)
    story_lang   = run_tags.get("storyLangCode", "ko")
    same_lang    = parent_code.lower() == story_lang.lower()

    pages = story_json.get("contents", [])
    seq_list = [p["sequence"] for p in pages]

    # 번역해야 할 페이지 목록을 명시적으로 나열
    page_lines = "\n".join(
        f'  - sequence {p["sequence"]}: "{p.get("contentKo", "")}"'
        for p in pages
    )

    if same_lang:
        rule = (
            f"원문 언어({story_lang})와 번역 목표 언어({parent_lang})가 동일합니다.\n"
            f"contentForeign = contentKo 를 그대로 복사하고, titleForeign = title 을 그대로 복사하세요."
        )
    else:
        rule = (
            f"모든 contentForeign 을 반드시 {parent_lang} ({parent_code}) 로 번역하세요.\n"
            f"한국어·원문 언어가 contentForeign 에 한 글자도 남으면 안 됩니다.\n"
            f"titleForeign 도 반드시 {parent_lang} 로 번역하세요."
        )

    return f"""당신은 어린이 동화 번역가입니다.
아래 동화의 각 페이지를 {parent_lang} ({parent_code}) 로 번역하세요.

## 번역 규칙
{rule}

## 반드시 번역해야 할 페이지 목록 (총 {len(seq_list)}개, sequence: {seq_list})
{page_lines}

## 번역 품질 규칙
- 어린이 동화 말투로 자연스럽게 번역한다.
- 원문에 없는 내용을 추가하거나 삭제하지 않는다.
- 주인공 이름과 고유명사는 음차 또는 원어 유지 중 자연스러운 쪽을 선택한다.

## 동화 원문 제목: {story_json.get("title", "")}

## 출력 형식 (JSON만, 마크다운 없이)
- contents 개수는 반드시 {len(seq_list)}개여야 한다.
{{
  "titleForeign": "{parent_lang} 번역 제목",
  "contents": [
    {{"sequence": 1, "contentForeign": "{parent_lang} 번역 본문"}}
  ]
}}"""
