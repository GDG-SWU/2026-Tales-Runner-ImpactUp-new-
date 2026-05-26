import os
import time
from typing import Any, Dict

from config.settings import TEXT_MODEL, MAX_RETRIES, RETRY_DELAY_BASE, RETRYABLE_CODES
from utils.json_utils import parse_json_response, print_stage


class GeminiService:
    """
    google-genai 클라이언트 래퍼.
    - JSON 응답 자동 파싱
    - 지수 백오프 재시도 (429 / 500 / 503)
    - 이미지 생성 지원
    """

    def __init__(self, api_key: str | None = None):
        resolved_key = api_key or os.environ.get("GEMINI_API_KEY", "")
        if not resolved_key:
            raise ValueError(
                "GEMINI_API_KEY가 설정되지 않았습니다.\n"
                "  os.environ['GEMINI_API_KEY'] = 'YOUR_KEY'  로 설정하세요."
            )
        try:
            from google import genai
            from google.genai import types as genai_types
        except ImportError:
            raise ImportError("google-genai 패키지를 설치하세요: pip install google-genai")

        self._types = genai_types
        self.client = genai.Client(api_key=resolved_key)

    # ── 내부 재시도 헬퍼 ────────────────────────────────────────────────────

    @staticmethod
    def _is_retryable(err: Exception) -> bool:
        msg = str(err)
        return any(str(code) in msg for code in RETRYABLE_CODES)

    def _run(self, stage: str, fn):
        last_err = None
        max_attempts = max(MAX_RETRIES, 5)   # 503 대비 최소 5회 보장
        for attempt in range(1, max_attempts + 1):
            try:
                return fn()
            except Exception as e:
                last_err = e
                if attempt < max_attempts and self._is_retryable(e):
                    # 지수 백오프: 10s → 20s → 40s → 80s → ...
                    wait = min(10 * (2 ** (attempt - 1)), 120)
                    print(f"[{stage}] 503/429 오류 → {wait}초 대기 후 재시도 ({attempt}/{max_attempts})")
                    time.sleep(wait)
                else:
                    break
        raise RuntimeError(f"[{stage}] 최종 실패 ({max_attempts}회 시도): {last_err}")

    # ── JSON 호출 ────────────────────────────────────────────────────────────

    def call_json(
        self,
        prompt: str,
        stage: str,
        temperature: float = 0.5,
        model: str = TEXT_MODEL,
    ) -> Dict[str, Any]:
        print_stage(f"{stage} - PROMPT", prompt)

        def _call():
            resp = self.client.models.generate_content(
                model=model,
                contents=prompt,
                config=self._types.GenerateContentConfig(
                    response_mime_type="application/json",
                    temperature=temperature,
                ),
            )
            raw = (resp.text or "").strip()
            print_stage(f"{stage} - RAW", raw)
            return parse_json_response(raw)

        result = self._run(stage, _call)
        print_stage(f"{stage} - PARSED", result)
        return result

    # ── 이미지 생성 ──────────────────────────────────────────────────────────

    def generate_images(self, model: str, prompt: str, **kwargs):
        return self._run(
            "Imagen",
            lambda: self.client.models.generate_images(model=model, prompt=prompt, **kwargs),
        )
