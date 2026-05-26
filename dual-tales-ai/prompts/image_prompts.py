import json
from typing import Any, Dict

# ── 공통 스타일 ─────────────────────────────────────────────────────────────
# 목표 화풍:
#   - 파스텔 톤 + 종이/캔버스 질감 (번쩍이는 AI 느낌 배제)
#   - gouache + 수채화 혼합, 거친 붓 터치
#   - 따뜻하고 부드러운 빛 (랜턴, 별빛, 자연광)
#   - 귀엽고 둥근 캐릭터, 레이어드 자연 배경
STYLE = (
    "children's picture book illustration, "
    "hand-painted gouache and watercolor mixed media, "
    "visible dry brushstrokes on textured paper, "
    "heavy canvas grain texture visible throughout entire image, rough linen-like surface, "
    "soft watercolor washes with gentle color bleeding at edges, translucent layered washes, "
    "muted pastel color palette — dusty rose, sage green, warm cream, soft lavender, peachy orange, "
    "desaturated earthy tones with gentle warmth, "
    "no glossy sheen, no digital airbrush smoothness, "
    "soft diffused natural lighting, subtle warm glow, "
    "rounded cute characters with expressive faces, "
    "lush layered natural backgrounds, "
    "cozy nostalgic storybook atmosphere, "
    "no frame, no border, no decorative border, no arch frame, full bleed illustration, "
    "non-photorealistic, no 3D rendering, no CGI, no flat vector"
)

# ── 텍스트 금지 ──────────────────────────────────────────────────────────────
NO_TEXT = (
    "pure illustration only, absolutely no text, no letters, no numbers, "
    "no words, no symbols, no signs, no labels, no captions, no speech bubbles, "
    "no written characters of any language, text-free image"
)

# ── 기본 금지 목록 (최소화 — 과부하 방지) ────────────────────────────────────
AVOID_BASE = [
    "3D render", "CGI", "photorealistic",
    "flat vector", "digital airbrush", "glossy plastic sheen",
    "saturated neon colors", "dark horror mood",
    "text", "letters", "words", "numbers", "symbols",
    "signs", "labels", "speech bubbles", "written characters",
    "frame", "border", "decorative border", "arch frame",
]

# ── 표지 전용 금지 목록 (간결하게) ───────────────────────────────────────────
AVOID_COVER = [
    "3D render", "CGI", "photorealistic",
    "flat vector", "glossy sheen", "neon colors",
    "text", "letters", "words", "numbers", "symbols",
    "book title", "signs", "labels", "written characters",
    "frame", "border", "decorative border", "arch frame",
]

FALLBACK_STYLE = (
    "children's picture book illustration, "
    "hand-painted gouache on heavy canvas texture, rough linen-like surface, "
    "soft watercolor washes with gentle color bleeding, "
    "muted pastel colors, soft dry brushstrokes, "
    "warm diffused light, cute rounded characters, "
    "cozy storybook mood, no frame, no border, full bleed illustration, "
    "pure illustration, text-free"
)

# ── 장면별 빛 분위기 힌트 ──────────────────────────────────────────────────
def _light_hint(page: Dict[str, Any]) -> str:
    scene = (page.get("sceneSummary", "") or page.get("contentKo", "")).lower()
    if any(k in scene for k in ["밤", "별", "달", "어둠", "night", "star", "moon"]):
        return "soft moonlit night, dusty blue-purple sky, warm lantern glow, gentle starlight"
    if any(k in scene for k in ["아침", "새벽", "sunrise", "morning"]):
        return "pale golden morning haze, soft peachy-pink sunrise, gentle warm light"
    if any(k in scene for k in ["숲", "나무", "forest", "wood"]):
        return "dappled sage-green forest light, soft ambient glow filtering through leaves"
    if any(k in scene for k in ["반딧불", "빛", "빛나", "glow", "firefly", "lantern"]):
        return "warm golden bioluminescent sparkles, soft creamy light particles, gentle glow"
    return "warm soft afternoon light, gentle natural illumination, no harsh shadows"


def _char_anchor(story_state: Dict[str, Any]) -> str:
    char = story_state.get("character", {})
    name = char.get("name", "the main character")
    trait = char.get("unique_trait", "")
    appearance = char.get("appearance", "")

    desc = f"CHARACTER: {name}. Keep IDENTICAL on every page: same face, body proportions, color scheme, silhouette."
    if appearance:
        desc += f" Appearance: {appearance}."
    if trait:
        desc += f" Must always show: {trait}."
    return desc


# ── 본문 페이지 프롬프트 ───────────────────────────────────────────────────
def build_page_image_prompt(
    story_state: Dict[str, Any],
    story_title: str,
    run_tags: Dict[str, Any],
    page: Dict[str, Any],
) -> str:
    char_anchor = _char_anchor(story_state)
    scene = page.get("sceneSummary", "") or page.get("contentKo", "")[:120]
    light = _light_hint(page)

    return f"""당신은 Imagen 4용 어린이 동화 삽화 프롬프트 작성자입니다.
아래 장면을 영어 이미지 프롬프트로 변환하세요.

## 목표 화풍
파스텔 톤 + 종이 질감의 gouache·수채화 그림체. 번쩍이는 디지털 느낌 금지.
예시 분위기: 랜턴을 든 소녀와 반딧불, 숲속 동물 티파티, 곰이 세탁물을 너는 아늑한 마당.

## 규칙
- 프롬프트는 반드시 영어로 작성 (300 tokens 이내)
- 이미지 안에 글자·숫자·기호·표지판·라벨 등 어떠한 문자도 절대 포함 금지 — 순수 삽화만
- glossy·shiny·vibrant 계열 표현 사용 금지 — muted·soft·dusty 계열 사용
- 3D/CGI/포토리얼/플랫벡터 금지

## 스타일 (모든 페이지 동일)
{STYLE}

## 캐릭터 (변경 불가)
{char_anchor}

## 이번 장면
- 동화 제목: {story_title}
- 페이지: {page["sequence"]}
- 장면 요약: {scene}
- 빛 분위기: {light}

## 프롬프트 구조
[gouache on textured paper, muted pastel tones, dry brushstrokes] + [character description] + [scene action] + [lush natural background] + [light mood: {light}] + [cozy storybook feel, text-free]

## 출력 형식 (JSON만, 마크다운 없이)
{{
  "sequence": {page["sequence"]},
  "imagePrompt": "영어 이미지 프롬프트",
  "avoid": {json.dumps(AVOID_BASE)}
}}"""


# ── 표지 전용 프롬프트 ─────────────────────────────────────────────────────
def build_cover_image_prompt(
    story_state: Dict[str, Any],
    story_title: str,
    title_foreign: str,
    run_tags: Dict[str, Any],
) -> str:
    """
    표지 이미지 전용 프롬프트.
    - 캐릭터를 중앙에 크게 배치 (person_generation은 image_service에서 allow_adult로 처리)
    - 파스텔 톤 + 종이질감 스타일 일관성 유지
    - avoid 목록 최소화로 안전 필터 과부하 방지
    """
    char_anchor = _char_anchor(story_state)
    setting     = story_state.get("setting", {})
    place       = setting.get("place", "a magical world")
    sensory     = setting.get("sensory_detail", "")
    visual      = setting.get("visual_anchor", "")
    plot        = story_state.get("plot", {})
    incident    = plot.get("incident", "")
    resolution  = plot.get("resolution", "")
    emotion     = story_state.get("emotion", {})
    emotion_word = emotion.get("expression", "warm and hopeful")

    setting_desc = f"Setting: {place}."
    if sensory:
        setting_desc += f" Atmosphere: {sensory}."
    if visual:
        setting_desc += f" Key visual: {visual}."

    story_moment = ""
    if incident:
        story_moment += f"Conflict: {incident}. "
    if resolution:
        story_moment += f"Resolution: {resolution}."
    if not story_moment:
        story_moment = f"The main character on a heartfelt adventure in {place}."

    return f"""당신은 Imagen 4용 어린이 동화 표지 삽화 프롬프트 작성자입니다.

## 목표 화풍
파스텔 톤 + 종이/캔버스 질감의 gouache·수채화 그림체. 번쩍이는 디지털 느낌 금지.
표지는 주인공이 크고 선명하게, 배경은 서정적으로.

## 규칙
- 프롬프트는 영어로 작성 (300 tokens 이내)
- 이미지 전체를 일러스트로 채울 것 — 글자·숫자·기호·제목·라벨 등 어떠한 문자도 절대 포함 금지
- glossy·shiny·vibrant 계열 표현 금지 — muted·soft·dusty 계열 사용
- 3D/CGI/포토리얼/플랫벡터 금지

## 스타일
{STYLE}

## 캐릭터
{char_anchor}

## 배경 정보
{setting_desc}

## 핵심 장면
{story_moment}

## 감정 톤
{emotion_word}

## 표지 구도
- 주인공 화면 중앙에 크고 선명하게
- 가장 감동적인 순간 한 컷으로 표현
- 앞·중·뒤 원근감 있는 레이어드 배경
- 파스텔 자연광 또는 부드러운 마법빛으로 장면 전체를 감쌀 것
- 이미지 가장자리까지 빈 공간 없이 일러스트로 꽉 채울 것

## 프롬프트 구조
[gouache on textured paper, muted pastel palette, visible dry brushstrokes] + [CHARACTER centered, expressive, large] + [most emotional story moment] + [lush layered background, foreground·midground·background depth] + [soft warm pastel light filling scene] + [cozy storybook cover, text-free]

## 출력 형식 (JSON만, 마크다운 없이)
{{
  "sequence": 0,
  "imagePrompt": "영어 이미지 프롬프트",
  "avoid": {json.dumps(AVOID_COVER)}
}}"""


# ── 폴백 프롬프트 ─────────────────────────────────────────────────────────
def fallback_page_prompt(story_state: Dict[str, Any], page: Dict[str, Any]) -> str:
    char = story_state.get("character", {})
    name = char.get("name", "a cute character")
    trait = char.get("unique_trait", "")
    scene = page.get("sceneSummary", "") or page.get("contentKo", "")[:80]
    char_desc = f"{name} with {trait}" if trait else name
    light = _light_hint(page)
    return (
        f"{FALLBACK_STYLE}, {char_desc} in a scene, "
        f"scene: {scene[:100]}, {light}, "
        f"lush natural background, warm cozy mood, "
        f"pure illustration, text-free"
    )


def fallback_cover_prompt(story_state: Dict[str, Any]) -> str:
    char = story_state.get("character", {})
    name = char.get("name", "a cute character")
    trait = char.get("unique_trait", "")
    place = story_state.get("setting", {}).get("place", "a magical world")
    char_desc = f"{name} with {trait}" if trait else name
    return (
        f"{FALLBACK_STYLE}, children's storybook cover illustration, "
        f"{char_desc} standing in {place}, "
        f"soft pastel warm light fills the entire scene, "
        f"lush layered background, full illustration edge to edge, "
        f"pure illustration, text-free, no letters"
    )
