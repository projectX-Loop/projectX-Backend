# Plan RAG 설계

## 경계와 API

호출 경계는 `Frontend → Backend → ai-service`다. 프론트엔드는 `ai-service`를 직접 호출하지 않는다.

백엔드가 `ai-service`에 호출하는 API는 두 개다.

| API | 용도 |
| --- | --- |
| `POST /calculate` | 저장된 plan 입력으로 시뮬레이션 결과를 재생성 |
| `POST /rag/ask` | 초기 설명 또는 후속 질문에 대한 RAG 응답 생성 |

`POST /rag/answer`는 사용하지 않는다. 초기 설명과 질문은 `POST /rag/ask` 하나에 `mode`를 지정해 처리한다.

## RAG 요청과 응답

백엔드는 RAG 요청마다 저장된 plan 입력으로 `/calculate`를 호출한다. 계산 결과는 별도로 저장하지 않는다.

`POST /rag/ask` 요청은 다음 필드를 사용한다.

| 필드 | 타입 | 규칙 |
| --- | --- | --- |
| `mode` | `EXPLANATION \| QUESTION` | 초기 설명 또는 후속 질문 구분 |
| `calculation` | object | 같은 요청에서 `/calculate`로 얻은 결과 |
| `question` | string | `mode=QUESTION`일 때만 포함 |
| `history` | `{ question: string, answer: object }[]` | 최근 성공 질문·답변 5개를 오래된 순서로 포함 |

응답은 `status`, 성공 결과, `retrieved_refs`를 포함한다. `EXPLANATION`의 상태는 `OK`, `EXPLANATION_REJECTED`, `EXPLANATION_UNAVAILABLE`이고, `QUESTION`의 상태는 `OK`, `ANSWER_REJECTED`, `ANSWER_UNAVAILABLE`이다. 성공 결과는 설명 또는 답변 claim이며, 실패 결과는 사용자 표시용 `message`다.

## 공개 확장 API

프론트 공개 계약의 5개 API와 별도로, 대화 기능은 다음 API를 사용한다.

| API | 동작 |
| --- | --- |
| `POST /api/v1/plans/{public_id}/questions` | 질문을 받고 RAG 응답을 저장·반환 |
| `GET /api/v1/plans/{public_id}/questions` | 저장된 질문·응답을 생성 순서대로 반환 |

초기 설명은 기존 `POST /api/v1/plans/{public_id}/explanation`이 담당한다. 같은 plan에서 `OK` 또는 `EXPLANATION_REJECTED` 결과가 있으면 이를 재사용한다. 결과가 없거나 가장 최근 결과가 `EXPLANATION_UNAVAILABLE`이면 다시 생성한다.

## 대화 저장

`plan_explanation`은 별도 세션 테이블 없이 `plan_id`를 하나의 대화 스레드로 사용한다. 하나의 plan에는 초기 설명 행과 여러 질문 행이 저장될 수 있다.

| 컬럼 | 규칙 |
| --- | --- |
| `id` | 기본 키 |
| `plan_id` | `plan.id` 외래 키, `NOT NULL`, plan 삭제 시 함께 삭제 |
| `kind` | `EXPLANATION` 또는 `QUESTION` |
| `question` | `QUESTION`일 때만 `NOT NULL`, `EXPLANATION`은 `NULL` |
| `status` | kind별 RAG 허용 상태 |
| `payload` | 성공이면 설명 또는 답변 claim, 실패면 `{ message }` |
| `retrieved_refs` | 인용한 지식 청크 배열, 기본값 `[]` |
| `created_at` | 생성 시각 |

질문 문맥은 `kind=QUESTION` 및 `status=OK`인 행만 대상으로 최근 5개를 조회한다. 조회한 행은 생성 시각 오름차순으로 `/rag/ask`에 전달한다.
