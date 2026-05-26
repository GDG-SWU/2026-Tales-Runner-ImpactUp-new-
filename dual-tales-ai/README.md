# Story Generator 

Gemini + Imagen 4 기반 어린이 맞춤 동화 생성 파이프라인 

## 파이프라인 단계

|단계|설명|
|-|-|
|STEP 1\~2|인터뷰 (질문 생성 + 답변 정제), 최대 12개|
|STEP 3|페이지별 동화 초안 생성|
|STEP 3.5|책 제목 생성 (후보 3개 → 최적 1개 선정)|
|STEP 4|동화 품질 검수 및 수정|
|STEP 5|부모 언어로 번역 (검증 + 자동 재시도)|
|STEP 6|페이지별 + 표지 Imagen 프롬프트 생성|
|STEP 7|Imagen 4 이미지 생성 (표지 2:3 + 본문 1:1)|

## 디렉토리 구조

```
story\_generator\_v2/
├── app.py
├── config/
│   └── settings.py          # 모델, 경로, 창의성, 재시도 설정
├── prompts/
│   ├── lang\_map.py          # 언어 코드 → 언어명 공통 매핑
│   ├── question\_prompts.py  # 인터뷰 질문 / 답변 정제 / 번역 보정
│   ├── story\_prompts.py     # 동화 생성 / 품질 검수
│   ├── title\_prompts.py     # 책 제목 생성
│   ├── translation\_prompts.py # 번역 (페이지 목록 명시)
│   └── image\_prompts.py     # 본문/표지 이미지 프롬프트
├── questions/
│   └── question\_queue.py    # 나이별 질문 풀 / 순서 결정
├── services/
│   ├── gemini\_service.py    # Gemini API 래퍼 (지수 백오프 재시도)
│   ├── image\_service.py     # Imagen 4 이미지 생성 + 표지 오버레이
│   └── pipeline.py          # 전체 파이프라인
├── state/
│   └── story\_state.py       # 동화 상태 초기화 / run\_tags 생성
└── utils/
    ├── json\_utils.py        # JSON 파싱 / 저장 / 출력
    └── state\_utils.py       # 중첩 딕셔너리 헬퍼
```

## 출력 파일

|파일|내용|
|-|-|
|`00\_run\_tags.json`|실행 파라미터|
|`01\_interview\_logs.json`|인터뷰 로그|
|`02\_story\_state.json`|수집된 동화 설정|
|`03\_story\_draft.json`|동화 초안|
|`03\_5\_title\_result.json`|제목 후보 및 선정 결과|
|`04\_story\_validated.json`|검수된 동화|
|`05\_story\_translated.json`|번역된 동화|
|`06\_story\_with\_image\_prompts.json`|이미지 프롬프트 포함 동화|
|`07\_final\_backend\_payload.json`|최종 결과물|



