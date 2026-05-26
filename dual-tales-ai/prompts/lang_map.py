"""언어 코드 → 언어명 매핑 (모든 프롬프트 파일에서 공유)."""

LANG_MAP = {
    "ko":    "Korean (한국어)",
    "en":    "English",
    "zh-CN": "Chinese Simplified (简体中文)",
    "zh-TW": "Chinese Traditional (繁體中文)",
    "zh-HK": "Chinese Traditional HK (繁體中文)",
    "ja":    "Japanese (日本語)",
    "es":    "Spanish (Español)",
    "fr":    "French (Français)",
    "de":    "German (Deutsch)",
    "vi":    "Vietnamese (Tiếng Việt)",
    "th":    "Thai (ภาษาไทย)",
    "ar":    "Arabic (العربية)",
    "ru":    "Russian (Русский)",
    "pt":    "Portuguese (Português)",
    "id":    "Indonesian (Bahasa Indonesia)",
    "hi":    "Hindi (हिन्दी)",
}


def lang_name(code: str) -> str:
    return LANG_MAP.get(code, code)
