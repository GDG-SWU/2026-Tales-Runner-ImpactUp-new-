from typing import Any, Dict


def get_age_group(age: int) -> str:
    if 3 <= age <= 5:
        return "3-5"
    if 6 <= age <= 8:
        return "6-8"
    raise ValueError(f"지원 나이 범위: 3~8세 (입력값: {age})")


def get_nested(data: Dict, path: str):
    cur = data
    for k in path.split("."):
        if not isinstance(cur, dict):
            return None
        cur = cur.get(k)
    return cur


def set_nested(data: Dict, path: str, value: Any):
    keys = path.split(".")
    cur = data
    for k in keys[:-1]:
        cur = cur.setdefault(k, {})
    cur[keys[-1]] = value


def deep_merge(base: Dict, update: Dict) -> Dict:
    if not isinstance(update, dict):
        return base
    for k, v in update.items():
        if isinstance(v, dict) and isinstance(base.get(k), dict):
            deep_merge(base[k], v)
        elif v not in (None, "", [], {}):
            base[k] = v
    return base
