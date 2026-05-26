import os
from pathlib import Path

# 모델 설정
TEXT_MODEL  = "gemini-2.5-flash"
FAST_MODEL  = "gemini-2.5-flash-lite"
IMAGE_MODEL = "imagen-4.0-generate-001"

# 출력 경로
def _default_output_dir() -> Path:
    if os.path.isdir("/content"):
        return Path("/content/story_outputs")
    return Path("story_outputs")

OUTPUT_DIR = _default_output_dir()
IMAGE_DIR  = OUTPUT_DIR / "images"   # 이미지 저장 폴더 (StaticFiles로 서빙)

# 인터뷰 제한
MAX_TOTAL_QUESTIONS = 12

# 디버그
VERBOSE = False

# API 재시도
MAX_RETRIES      = 3
RETRY_DELAY_BASE = 2
RETRYABLE_CODES  = {429, 500, 503}

# 창의성별 temperature 설정
CREATIVITY_CONFIG = {
    "low": {
        "question":    0.3,
        "refine":      0.2,
        "story":       0.5,
        "title":       0.4,
        "validation":  0.2,
        "translation": 0.2,
        "img_prompt":  0.3,
    },
    "medium": {
        "question":    0.5,
        "refine":      0.2,
        "story":       0.8,
        "title":       0.6,
        "validation":  0.2,
        "translation": 0.3,
        "img_prompt":  0.4,
    },
    "high": {
        "question":    0.7,
        "refine":      0.2,
        "story":       1.0,
        "title":       0.9,
        "validation":  0.2,
        "translation": 0.3,
        "img_prompt":  0.6,
    },
}

def get_creativity_config(level: str) -> dict:
    return CREATIVITY_CONFIG.get(level, CREATIVITY_CONFIG["medium"])
