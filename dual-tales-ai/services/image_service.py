import io
import uuid
from pathlib import Path
from typing import Optional

from config.settings import IMAGE_MODEL, IMAGE_DIR
from utils.json_utils import print_stage


# ── Colab 인라인 표시 ────────────────────────────────────────────────────────

def _display_colab(image_path: Path, caption: str = "") -> None:
    try:
        from IPython.display import display, HTML
        import base64
        b64 = base64.b64encode(image_path.read_bytes()).decode()
        ext = image_path.suffix.lstrip(".")
        html = (
            f'<div style="margin:16px 0;">'
            f'<div style="font-size:13px;color:#888;margin-bottom:6px;">{caption}</div>'
            f'<img src="data:image/{ext};base64,{b64}" '
            f'style="max-width:480px;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,.18);" />'
            f"</div>"
        )
        display(HTML(html))
    except Exception:
        pass


# ── 단일 이미지 생성 시도 ────────────────────────────────────────────────────

def _try_generate(gemini_service, prompt: str, aspect_ratio: str, label: str, is_cover: bool = False):
    """
    단일 프롬프트로 이미지 생성 시도.
    - is_cover=True 일 때 person_generation="allow_adult"
    - 빈 응답은 안전 필터 트리거 가능성 → None 반환
    """
    try:
        from google.genai import types

        safe_prompt = prompt[:1800]
        person_gen = "allow_adult" if is_cover else "dont_allow"

        response = gemini_service.generate_images(
            model=IMAGE_MODEL,
            prompt=safe_prompt,
            config=types.GenerateImagesConfig(
                number_of_images=1,
                aspect_ratio=aspect_ratio,
                person_generation=person_gen,
            ),
        )
        if response and response.generated_images:
            return response.generated_images[0].image

        print(f"[Imagen] {label} — 빈 응답 (안전 필터 가능성). 폴백 프롬프트로 재시도합니다.")
        return None
    except Exception as e:
        print(f"[Imagen] {label} 시도 실패: {e}")
        return None


# ── 이미지 객체 → PIL 변환 ───────────────────────────────────────────────────

def _to_pil(image_obj):
    """google.genai.types.Image → PIL Image"""
    from PIL import Image as PILImage
    raw_bytes = getattr(image_obj, "image_bytes", None)
    if raw_bytes:
        return PILImage.open(io.BytesIO(raw_bytes))
    return image_obj._pil_image


# ── 파일 저장 ────────────────────────────────────────────────────────────────

def _save_jpeg(pil_img, path: Path, quality: int = 85) -> Path:
    """PIL 이미지를 JPEG로 저장. 디렉토리 자동 생성."""
    IMAGE_DIR.mkdir(parents=True, exist_ok=True)
    pil_img.convert("RGB").save(str(path), format="JPEG", quality=quality, optimize=True)
    return path


# ── 메인 이미지 생성 함수 ────────────────────────────────────────────────────

def generate_image(
    gemini_service,
    prompt: str,
    sequence: int,
    aspect_ratio: str = "1:1",
    display_image: bool = True,
    fallback_prompt: Optional[str] = None,
    max_attempts: int = 2,
) -> Path:
    """
    Imagen 4로 이미지 1장 생성 후 로컬에 저장, Path 반환.

    파일명: cover_{uuid8}.jpg / page_{sequence}_{uuid8}.jpg
    UUID 8자리로 매 생성마다 고유한 파일명 보장 → 덮어쓰기 없음.

    - sequence == 0 : 표지(Cover), person_generation="allow_adult"
    - 원본 프롬프트 실패 시 fallback_prompt로 재시도
    - max_attempts 초과 시 RuntimeError 발생
    """
    is_cover = (sequence == 0)
    label    = "표지(Cover)" if is_cover else f"Page {sequence}"
    uid      = uuid.uuid4().hex[:8]  # 예: a3f2b1c4
    filename = f"cover_{uid}.jpg" if is_cover else f"page_{sequence}_{uid}.jpg"
    path     = IMAGE_DIR / filename

    print_stage(f"Imagen 생성 - {label}", prompt[:200])

    candidates = [("원본", prompt)]
    if fallback_prompt and fallback_prompt != prompt:
        candidates.append(("폴백", fallback_prompt))

    attempts = candidates[:max(1, int(max_attempts))]

    for i, (plabel, cur_prompt) in enumerate(attempts, 1):
        print(f"[Imagen] {label} - {plabel} 프롬프트 시도 ({i}/{len(attempts)})")
        img_obj = _try_generate(gemini_service, cur_prompt, aspect_ratio, label, is_cover=is_cover)
        if img_obj is not None:
            pil_img = _to_pil(img_obj)
            _save_jpeg(pil_img, path)
            if display_image:
                _display_colab(path, caption=label)
            print(f"[Imagen] {label} 저장 완료: {path}")
            return path

    raise RuntimeError(f"[Imagen] {label} 생성 실패: {len(attempts)}회 시도 초과")
