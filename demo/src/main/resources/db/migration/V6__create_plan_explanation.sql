-- plan_explanation — KAN-23·KAN-24 통합 (9/5 도윤·성종현 결정).
-- 세션 테이블(agent_session) 없이 plan_id로 바로 그룹핑한다. 대화 순서는 created_at.
-- kind=EXPLANATION 행은 plan당 보통 1개(POST /explanation 최초 호출), kind=QUESTION 행이 이어진다.
CREATE TABLE plan_explanation (
    id              BIGSERIAL PRIMARY KEY,
    plan_id         BIGINT      NOT NULL,
    kind            VARCHAR(16) NOT NULL,
    question        TEXT,
    status          VARCHAR(32) NOT NULL,
    payload         JSONB       NOT NULL,
    retrieved_refs  JSONB       NOT NULL DEFAULT '[]',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_plan_explanation_plan FOREIGN KEY (plan_id) REFERENCES plan (id) ON DELETE CASCADE,
    CONSTRAINT chk_plan_explanation_kind CHECK (kind IN ('EXPLANATION', 'QUESTION')),
    CONSTRAINT chk_plan_explanation_question CHECK (kind <> 'QUESTION' OR question IS NOT NULL)
);

CREATE INDEX idx_plan_explanation_plan_id_created_at ON plan_explanation (plan_id, created_at);
