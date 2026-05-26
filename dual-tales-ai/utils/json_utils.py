import json
import re
from pathlib import Path
from typing import Any, Dict

from config.settings import VERBOSE


def print_stage(title: str, data: Any = None):
    """VERBOSE=True일 때만 중간 결과를 출력한다."""
    if not VERBOSE:
        return
    print("\n" + "=" * 80)
    print(f"[{title}]")
    print("=" * 80)
    if data is None:
        return
    if isinstance(data, (dict, list)):
        print(json.dumps(data, ensure_ascii=False, indent=2))
    else:
        print(data)


def strip_json_markdown(text: str) -> str:
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?\s*", "", text).strip()
        text = re.sub(r"\s*```$", "", text).strip()
    return text


def parse_json_response(text: str) -> Dict[str, Any]:
    cleaned = strip_json_markdown(text)
    try:
        return json.loads(cleaned)
    except json.JSONDecodeError:
        match = re.search(r"\{[\s\S]*\}", cleaned)
        if match:
            return json.loads(match.group(0))
        raise ValueError(f"JSON 파싱 실패. 응답 원문:\n{cleaned[:500]}")


def save_json(path: Path, data: Any):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def remove_qa_fields(story_json: Dict[str, Any]) -> Dict[str, Any]:
    """최종 출력에서 question / answer 필드 제거."""
    for page in story_json.get("contents", []):
        page.pop("question", None)
        page.pop("answer", None)
    return story_json
