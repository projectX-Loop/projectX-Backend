# ProjectX Backend

금융 AI 챌린지 자산관리 MVP 백엔드 오케스트레이션 서버

## 1. Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 4.1.1
- **Build Tool**: Gradle
- **Database**: PostgreSQL 16
- **External Communication**: RestClient (FastAPI `ai-service` 연동)

## 로컬 PostgreSQL

`.env.example`을 복사해 `.env`를 만들고 모든 `POSTGRES_*` 값을 입력한 뒤 아래 명령으로 로컬 DB를 시작한다.

```bash
docker compose up -d postgres
cd demo && ./gradlew bootRun --args='--spring.profiles.active=local'
```

`compose.yaml`은 `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`를 사용하고,
`local` 프로필은 `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`를 사용한다.
두 설정의 DB명·사용자·비밀번호는 같아야 하며, 누락된 값이 있으면 시작하지 않는다. 기존 PostgreSQL 컨테이너 또는 볼륨의 인증 정보는 Compose 설정 변경만으로
바뀌지 않으므로, 기존 로컬 데이터를 보존해야 하면 백업 후 별도로 교체한다.

## AI 계산 서비스

계획 생성과 조회는 `POST /calculate`을 호출한다. AI 서비스 주소는 `AI_SERVICE_BASE_URL`로 설정하며, 로컬 기본값은
`http://localhost:8000`이다. 백엔드는 `PlanInputs`만 전달하고 계산 데이터를 직접 전송하지 않는다. AI 서비스는 MVP 스냅샷과
동일한 데이터 기준으로 계산하고, 응답의 `calculation.meta.data_hash`에 `sha256:mvp-2021-08-2026-07-v1`을 반환해야 한다.
해시가 누락되거나 현재 계획의 스냅샷과 다르면 백엔드는 계산 실패로 처리한다.

## 2. 공개 API 계약

프론트엔드 공개 API의 기본 경로는 `/api/v1`, 콘텐츠 타입은 `application/json`이다. 이 문서는 프론트 커밋 [`2bdd7b4`](https://github.com/projectX-Loop/frontend/tree/2bdd7b463a644e2a78614d9e5cdad3c285ccc61f/docs/api-contract.md)의 계약을 기준으로 한다. `public_id`는 `POST /plans`가 반환하는 UUID이며, 이후 조회·AI 설명 요청에 사용한다. RAG 호출과 대화 저장 설계는 [Plan RAG 설계](docs/plan-rag-design.md)에 정리한다.

현재 구현 상태는 계약과 별개다. `GET /universe`, `GET /samples`, `POST /plans`,
`GET /plans/{public_id}`는 구현되어 있다. `POST /plans/{public_id}/explanation`은 RAG 담당 작업으로 별도 진행한다.

### 공통 요청: `PlanInputs`

| 필드 | 타입 | 제약 |
| --- | --- | --- |
| `goal.amount` | number | KRW, 1,000,000~10,000,000,000 |
| `goal.horizon_months` | number | 12~120 |
| `funds.initial` | number | 음수 불가 |
| `funds.monthly` | number | 음수 불가, `initial`과 동시에 0 불가 |
| `alloc.initial.invest`, `.safe`, `.other` | number | 각각 0~100, 합계 100 |
| `alloc.monthly.invest`, `.safe`, `.other` | number | 각각 0~100, 합계 100 |
| `portfolio.assets` | `AssetWeight[]` | 1~3개, 코드 중복 불가, 비중 합계 100 |
| `portfolio.assets[].code` | string | `KR_EQ`, `US_EQ`, `KR_BOND`, `US_EQ_KR` 중 카탈로그 제공 코드 |
| `portfolio.assets[].weight` | number | 정수 %, `0 < weight ≤ 100` |
| `rebalancing.focus` | `"M" \| "Q" \| "H"` | 월·분기·반기 강조 주기 |

### `GET /universe`

입력 폼의 자산 카탈로그와 현재 데이터 기준을 반환한다. 요청 본문과 쿼리 파라미터는 없으며, `200 UniverseResponse`를 반환한다.

| 응답 필드 | 타입 | 설명 |
| --- | --- | --- |
| `snapshot.data_version`, `snapshot.data_hash` | string | 데이터 버전과 해시 |
| `snapshot.window` | `{ start: string, end: string, months: number }` | 데이터 기간 |
| `assets[]` | `UniverseAsset[]` | 선택 가능한 자산 |
| `assets[].code` | string | `PlanInputs.portfolio.assets[].code` 값 |
| `assets[].display_name`, `.instrument`, `.tax_class` | string | 표시명, 종목코드, 세금 분류 |

UX/UI는 `GET /api/v1/universe`가 반환한 `assets[]`로 자산 선택 목록을 구성한다. 화면에는
`display_name`과 `instrument`를 표시하고, 사용자가 고른 `code`와 입력한 `weight`만
`POST /api/v1/plans`의 `portfolio.assets[]`로 전송한다. 안전 금리는 카탈로그가 아니라
계산 결과의 `calculation.meta.safe_rate_annual_pct`에서 표시한다.

### `GET /samples`

대표 페르소나 입력값을 반환한다. 요청 본문과 쿼리 파라미터는 없으며, `200 { samples: Sample[] }`를 반환한다.

| 응답 필드 | 타입 | 설명 |
| --- | --- | --- |
| `samples[].id` | string | 샘플 식별자 (`P0` 등) |
| `samples[].label` | string | 화면 표시명 |
| `samples[].inputs` | `PlanInputs` | 그대로 `POST /plans`에 보낼 입력 |

### `POST /plans`

`PlanInputs`를 저장하고 월·분기·반기 시뮬레이션을 계산한다. 성공 시 `201 PlanResponse`를 반환한다. 오류는 `400`, `500`, `502`이며 본문은 `ErrorEnvelope`다. `502 CALCULATION_FAILED`에는 재계산용 `public_id`를 포함한다.

### `GET /plans/{public_id}`

저장된 입력으로 결과를 재계산한다. 경로 파라미터 `public_id`는 UUID이며, 요청 본문과 쿼리 파라미터는 없다. 성공 시 `200 PlanResponse`, 실패 시 `404 PLAN_NOT_FOUND` 또는 `502 CALCULATION_FAILED`의 `ErrorEnvelope`를 반환한다.

### `POST /plans/{public_id}/explanation`

계산 결과 기반 AI 설명을 생성한다. 경로 파라미터 `public_id`는 UUID이며, 요청 본문과 쿼리 파라미터는 없다. 성공 응답은 항상 `200 ExplanationResponse`이고, AI 처리 성공 여부는 HTTP 상태가 아닌 `status`로 판단한다. 계획이 없으면 `404 PLAN_NOT_FOUND`, AI 연결 실패면 `502 EXPLANATION_UNAVAILABLE`의 `ErrorEnvelope`를 반환한다.

| `status` | `explanation` | `message` |
| --- | --- | --- |
| `OK` | `Explanation` | `null` |
| `EXPLANATION_REJECTED` | `null` | string |
| `EXPLANATION_UNAVAILABLE` | `null` | string |

### 공통 성공 응답

`PlanResponse`는 `POST /plans`, `GET /plans/{public_id}`가 공통으로 사용한다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `plan.public_id` | string (UUID) | 공개 식별자 |
| `plan.data_snapshot_id` | number | 생성 시점 스냅샷 ID |
| `plan.created_at` | string (ISO 8601) | 생성 시각 |
| `plan.inputs` | `PlanInputs` | 저장된 원본 입력 |
| `calculation.status` | string | 계산 상태 |
| `calculation.meta` | `Meta` | 데이터·가정·생성 기준 |
| `calculation.derived` | `Derived` (선택) | 파생 지표 |
| `calculation.per_period` | `Record<"M" \| "Q" \| "H", PeriodResult>` | 주기별 결과 |

- `Meta` 필수 필드: `assumptions_version`, `data_version`, `data_hash`, `window`, `data_basis`, `generated_at`, `safe_rate_annual_pct`, `start_month`, `target_month`, `cashflow_source`, `options`. `assets_used`, `warnings`, `series_used`는 선택이며 `safe_rate_annual_pct`, `options`는 `null` 가능하다.
- `Derived`: `propensity_label: string`, `invest_share_overall_pct: number | null`, `plan_excluded_amount: number | null`.
- `PeriodResult`: `trajectory?: TrajectoryPoint[]`, `cum_cost: number`, `risk: Risk`, `gap: Gap`, `tax: Tax | null`.
- `TrajectoryPoint`: `month`, `invest`, `safe`, `total`은 모두 number.
- `Risk`: `mdd_pct`, `vol_annual_pct`는 number이고 `worst_month_pct`, `max_drift_pct`는 `number | null`.
- `Gap`: `fv_total`, `shortfall`은 number이며 `extra_monthly_required`, `months_extension`, `months_extension_raw`, `extension_status`, `extra_monthly_ratio`, `status`, `basis`, `delta_m_model`은 `null` 가능하다.
- `Tax`: `realized_cum`, `fv_after_tax`는 `number | null`.

`Explanation`은 `status=OK`일 때만 존재한다.

| 필드 | 타입 |
| --- | --- |
| `summary`, `assumptions_note` | `Claim` |
| `per_period_pros_cons` | `Record<Period, ProsCons>` |
| `risks` | `RiskClaim[]` |
| `next_actions` | `NextAction[]` |
| `highlighted_period` | `Period | null` |
| `retrieved_refs` | string[] |

- `Claim`: `{ text: string, evidence: string[] }`
- `ProsCons`: `{ pros: Claim[], cons: Claim[] }`
- `RiskClaim`: `{ title: string, detail: string, evidence: string[] }`
- `NextAction`: `{ adjustable_input: "MONTHLY_CONTRIBUTION" | "GOAL_HORIZON" | "GOAL_AMOUNT" | "ALLOC_MONTHLY" | "ALLOC_INITIAL" | "REBALANCING_FOCUS", text: string, evidence: string[] }`

### 공통 오류 응답: `ErrorEnvelope`

모든 `4xx`·`5xx` 응답은 아래 구조를 사용한다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `code` | string | `VALIDATION_ERROR`, `PLAN_NOT_FOUND`, `CALCULATION_FAILED` 등 |
| `message` | string | 사용자 표시 메시지 |
| `retryable` | boolean | `true`면 재시도, `false`면 입력 수정 |
| `field` | string \| null | 단일 필드 오류의 점 표기 경로 |
| `errors` | `ErrorDetail[]` (선택) | 검증 오류 목록 |
| `public_id` | string \| null | 계산 실패 후 재시도할 plan 식별자 |
| `max_months` | number \| null | `INSUFFICIENT_HISTORY`의 가능한 최대 기간 |

`ErrorDetail`은 `{ code: string, field: string | null, message: string }`이다.

## 3. Branch Strategy
Git 브랜치는 `master`, `develop`, `feat` 3단계로 분기하며, 선형 히스토리를 위해 **Rebase and Merge**를 기본 병합 방식으로 채택합니다.

- `master`: 상용/데모 배포 브랜치 (EC2 환경에서 pull 및 docker compose 배포 대상)
- `develop`: 개발 통합 및 검증 브랜치
- `feat/{기능명}`: 단위 기능 개발 브랜치 (`develop`에서 분기)
