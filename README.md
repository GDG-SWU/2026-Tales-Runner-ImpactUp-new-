  # 2026-Tales-Runner-ImpactUp
# 📚 Dual Tales

> 다문화 가정 아이와 부모가 함께 만드는 AI 이중언어 맞춤형 동화 서비스

<br>

## 📖 서비스 소개

다문화 가정에서 언어와 문화 차이로 발생하는 교육 격차를 해소하기 위해 기획된 서비스입니다.
부모와 아이가 함께 인터뷰에 참여하면, AI가 아이의 관심사와 나이를 반영한 맞춤형 동화를 한국어와 부모 언어 두 가지로 자동 생성합니다.

<br>

## 🎯 목표

| 목표 | 설명 |
|------|------|
| 언어 격차 해소 | 한국어와 부모 언어를 동시에 제공해 자연스러운 이중 언어 학습 환경 조성 |
| 학습 접근성 향상 | 별도의 교육 자료 없이 가정에서 쉽게 사용할 수 있는 맞춤형 콘텐츠 제공 |
| 가족 간 소통 강화 | 인터뷰 참여와 동화 읽기를 통해 부모와 아이 간 자연스러운 대화 유도 |


> 🌱 **SDGs 4** 양질의 교육 &nbsp;|&nbsp; 🤝 **SDGs 10** 불평등 감소

<br>

## ✨ 핵심 기능

- 🎙️ **맞춤형 인터뷰** — 아이 나이에 따라 질문을 AI가 동적으로 생성 (최대 12개)
- 📖 **동화 자동 생성** — 답변 기반으로 연령별 어휘·문장 수준에 맞는 동화 및 삽화 생성
- 🌍 **이중언어 번역** — 동화 본문과 질문을 한국어와 부모 언어로 동시 제공

<br>

## 🏗️ 시스템 아키텍처

```
Android App (Kotlin)
        ↕ Retrofit + JWT
Spring Boot Backend (Java 17)
        ↕ HTTP
FastAPI AI Server (Python)
        ↕
Gemini API / Imagen 4
```

<br>

## 🤖 AI

| 분류 | 내용 |
|------|------|
| 서버 프레임워크 | FastAPI |
| AI 모델 | Gemini 2.5 Flash, Gemini 2.5 Flash Lite, Imagen 4 |
| 개발 환경 | Google Colab |

- 나이(3 ~ 5 / 6 ~ 8세)에 맞는 인터뷰 질문 동적 생성 및 한국어·부모 언어 동시 출력
- LLM 기반 답변 정제로 동화 구성 요소 누적 관리
- 연령 적합성·교육적 가치·서사 일관성 자동 품질 검수
- Imagen 4 기반 표지 및 페이지별 삽화 자동 생성

<br>

## 📱 Android

| 분류 | 내용 |
|------|------|
| 언어 및 IDE | Kotlin, Android Studio |
| 네트워크 | Retrofit, OkHttp |
| 인증 | JWT 자동 삽입 인터셉터 |

- 전체 화면 구성 및 BottomNavigationView 탭 네비게이션 설계
- 동화 생성 단계별 UI 및 로딩 화면 구현
- 페이지 페이드 전환·언어 전환 애니메이션·다크 모드 대응
- Retrofit + OkHttp 기반 백엔드 API 연동 구조 구축

<br>

## ⚙️ Backend

| 분류 | 내용 |
|------|------|
| 언어 및 프레임워크 | Java 17, Spring Boot 3.x |
| 인증 및 보안 | Spring Security, JWT, BCrypt |
| 데이터베이스 | MySQL, Spring Data JPA, Hibernate |
| 클라우드 및 배포 | GCP, Cloud Run, Secret Manager, Docker |
| API 문서화 | Swagger UI (Springdoc OpenAPI 2.x) |

- 회원·동화·공유 피드 도메인 ERD 설계 및 JPA 구성
- JWT 인증 + BCrypt 기반 회원 인증 API
- 동화 단계별 생성 흐름 CRUD API 및 권한 검증
- FastAPI AI 서버 HTTP 통신 연동
- Docker 컨테이너화 및 Google Cloud Run 배포
