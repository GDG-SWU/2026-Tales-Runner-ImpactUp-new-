"""
══════════════════════════════════════════════════════════
Story Generator v2 — 어린이 동화 자동 생성 파이프라인
══════════════════════════════════════════════════════════

Google Colab 실행 예시
──────────────────────
# 셀 1 — 패키지 설치
!pip install google-genai pillow -q

# 셀 2 — 압축 해제 & 경로 설정
import zipfile, os, sys
with zipfile.ZipFile("story_generator_v2.zip", "r") as z:
    z.extractall("/content")
os.chdir("/content/story_generator_v2")
sys.path.insert(0, "/content/story_generator_v2")

# 셀 3 — API 키 설정
import os
os.environ["GEMINI_API_KEY"] = "YOUR_KEY"

# 셀 4 — 파이프라인 실행
from services.pipeline import run_story_pipeline

result = run_story_pipeline(
    target_age=5,
    parent_lang_code="en",       # 부모 언어 코드
    story_lang_code="ko",        # 동화 원문 언어
    creativity_level="medium",
    generate_images=True,
    display_images=True,
    no_moral=False,
    skip_validation=False,
    use_demo_answers=False,      # True: 자동 데모 답변으로 빠른 테스트
)

# 셀 5 — 결과 확인
from IPython.display import display, HTML, Image

display(HTML(f"<h2>📖 {result['title']}</h2><p><i>{result.get('titleForeign','')}</i></p>"))
if result.get("coverImageUrl"):
    display(Image(filename=result["coverImageUrl"]))

for page in result["contents"]:
    display(HTML(f"<hr><b>Page {page['sequence']}</b>"))
    display(HTML(f"<p>{page['contentKo']}</p>"))
    if page.get("contentForeign"):
        display(HTML(f"<p style='color:#555'>{page['contentForeign']}</p>"))
    if page.get("imageUrl"):
        display(Image(filename=page["imageUrl"]))
══════════════════════════════════════════════════════════
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from services.pipeline import run_story_pipeline


if __name__ == "__main__":
    result = run_story_pipeline(
        target_age=5,
        parent_lang_code="en",
        story_lang_code="ko",
        creativity_level="medium",
        generate_images=False,   # 로컬 테스트 시 False
        display_images=False,
        no_moral=False,
        use_demo_answers=True,   # 빠른 테스트용 자동 답변
    )

    print("\n최종 결과:")
    print(f"  제목: {result.get('title')}")
    print(f"  번역: {result.get('titleForeign')}")
    for p in result.get("contents", []):
        print(f"\n  Page {p['sequence']}")
        print(f"    {p.get('contentKo','')[:60]}...")
        if p.get("contentForeign"):
            print(f"    {p['contentForeign'][:60]}...")
