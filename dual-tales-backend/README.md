# 📚 Dual Tales - Backend Service

> **AI 기반 다문화 가정을 위한 이중언어 동화 제작 서비스**
> 본 프로젝트는 **SDGs 4(양질의 교육)** 및 **SDGs 10(불평등 감소)** 가치를 실현하기 위해, 다문화 가정 내 언어 격차를 해소하고 아동의 문해력 향상을 돕는 AI 기반 이중언어 동화책 생성 플랫폼의 백엔드 서비스입니다.

---

## 🛠 Tech Stack

* **Framework:** Java 17, Spring Boot 4.0.6, Spring Data JPA
* **Database:** MySQL
* **Build Tool:** Gradle
* **Infrastructure:** Google Cloud Run, Cloud Build
* **External API Interworking:** FastAPI (Python AI Server Engine)

---

## 🏗 System Architecture & Data Flow

Spring Boot 프레임워크를 중심으로 동화 생성 엔진인 Python FastAPI 인프라와 느슨한 결합(Loose Coupling) 구조로 연동하여 안정적인 멀티 서비스 아키텍처를 구현했습니다.

```
[Android Client] 
       │
       ▼ (REST API)
[Spring Boot Backend] ──(HTTP POST /v1/ai/generate)──► [FastAPI AI Engine]
       │                                                      │
       ▼ (Persistence)                                        ▼ (Gen-AI)
  [MySQL DB]                                           [Gemini & Imagen API]

```

---

## 📊 Database ERD

---

## 🚀 Key Features & Implementation Details

### 1. 부모-자녀 대화 기반 스토리 빌더 (Story Draft 세션 관리)

* **비동기 상태 유지를 위한 설계:** 최초 질문 요청(`QUESTION`)부터 사용자의 답변을 수집하여 연속적인 문맥 질문을 이어가는 멀티 턴(Multi-turn) 대화 흐름을 제어합니다.
* **데이터 직렬화 및 복원:** AI 서버와의 매끄러운 세션 유지를 위해 고안된 컨텍스트 스냅샷 데이터(`story_state`)를 `Jackson ObjectMapper`를 활용하여 JSON String 포맷으로 직렬화한 뒤 MySQL에 동적으로 적재 및 복원합니다.
* **대외 안정성 확보:** 클라이언트의 요청 상태에 따라 공백 문자 예외 처리 및 타입 검증 로직을 구현하여 입력 데이터의 유효성을 보장합니다.

### 2. AI 기반 이중언어 동화책 최종 생성 및 무결성 검증

* **트랜잭션 기반 동화 최적화:** AI 서비스 인터페이스로부터 최종 완료(`isFinal: true`) 이벤트를 수신하면, 백엔드가 주도적으로 최종 동화책 생성을 요청(`STORY`)하여 수신된 원자적 데이터를 `Story` 및 `StoryContent` 엔티티로 변환 후 수동/자동 영속화(Persistence)를 수행합니다.
* **엄격한 데이터 무결성 검사:** AI 엔진이 전달한 설정 페이지 수(`pageCount`)와 실제 영속화 계층에 도달한 데이터 개수(`contentSize`)를 상호 교차 검증하는 방어적 예외 처리 로직(`IllegalArgumentException`)을 구현했습니다.
* **성공적인 가비지 컬렉션:** 최종 저장이 완료된 임시 드래프트 세션은 즉시 데이터베이스에서 삭제(`delete`) 처리하여 불필요한 스토리지 낭비를 원천 차단합니다.

### 3. 멀티랭귀지 번역 및 미디어 자원 매핑

* **이중언어 동시 서빙:** 다문화 가정의 특성을 고려하여 한국어(`KO`) 본문 데이터와 사용자 설정 타깃 모국어(예: `EN` 등)가 정교하게 매핑된 이중언어 텍스트 데이터를 프론트엔드에 확장성 있는 DTO 구조로 포맷팅하여 전달합니다.
* **CDN 및 미디어 매핑:** Imagen 및 Gemini를 통해 실시간으로 렌더링된 고화질 동화 삽화 및 표지 이미지 원격 자원 URL을 가공하여 상세 조회(`GET /api/stories/{id}`) 요청 시 모바일 클라이언트가 즉각 렌더링할 수 있도록 고성능 인덱싱 구조를 지원합니다.

---

## 🔍 API Specification (Core)

| Method | End Point | Description | Status |
| --- | --- | --- | --- |
| **POST** | `/api/story-drafts` | 최초 대화 및 드래프트 동화 생성 시작 | `201 Created` |
| **POST** | `/api/story-drafts/{id}/proceed` | 사용자 답변 전송 및 다음 턴 질문/최종본 수신 | `200 OK` |
| **GET** | `/api/stories` | 유저가 생성 완료한 동화책 전체 목록 조회 | `200 OK` |
| **GET** | `/api/stories/{id}` | 특정 동화책 상세 조회 (이중언어 본문 및 AI 삽화 포함) | `200 OK` |
| **POST** | `/api/stories` | 외부 및 테스트용 동화책 직접 수동 생성 수신 | `201 Created` |

---

## 🛠 Troubleshooting Experience

* **문제 상황:** 타 서버(FastAPI) 연동 중 데이터 전송 규격 미스매치로 인해 원격 서버 단에서 `400 Bad Request` 및 `422 Unprocessable Entity` 지속 발생
* **원인 분석:** 1. 외부 API 스키마 검증 시 문자열 대소문자 오타 오인 (`QUESTiON` 미스매치)
2. 파이썬 Pydantic 엔진의 엄격한 데이터 딕셔너리 검증 과정에서 빈 `HashMap` 데이터 구조 충돌 현상 발견
* **해결 방안:** - 요청 타입 필드의 상수를 대문자 정격 규격(`QUESTION`, `STORY`)으로 리팩토링 진행.
* 컨텍스트 상태가 없는 최초 적재 시점 및 복원 실패 시점에 명시적으로 빈 객체 대신 `null` 주입 방식을 채택하여 API 데이터 바인딩 오류를 우회하고 통신 신뢰도를 100%로 끌어올림.
