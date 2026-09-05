CREATE TABLE inv_asset (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    instrument      VARCHAR(128) NOT NULL,
    display_name    VARCHAR(128) NOT NULL,
    currency_code   VARCHAR(8)   NOT NULL,
    tax_class       VARCHAR(32)  NOT NULL,
    CONSTRAINT uk_inv_asset_code UNIQUE (code)
);

CREATE TABLE data_snapshot (
    id              BIGSERIAL PRIMARY KEY,
    data_version    VARCHAR(64)  NOT NULL,
    data_hash       VARCHAR(128) NOT NULL,
    start_month     DATE         NOT NULL,
    latest_month    DATE         NOT NULL,
    frozen          BOOLEAN      NOT NULL DEFAULT FALSE,
    is_current      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_data_snapshot_data_hash UNIQUE (data_hash)    
);

CREATE UNIQUE INDEX uq_data_snapshot_single_current ON data_snapshot (is_current) WHERE is_current;

CREATE TABLE data_snapshot_asset (
    data_snapshot_id      BIGINT NOT NULL,
    inv_asset_id          BIGINT NOT NULL,
    available_from_month  DATE   NOT NULL,
    available_to_month    DATE   NOT NULL,
    PRIMARY KEY (data_snapshot_id, inv_asset_id),
    CONSTRAINT fk_dsa_data_snapshot FOREIGN KEY (data_snapshot_id) REFERENCES data_snapshot (id) ON DELETE CASCADE,
    CONSTRAINT fk_dsa_inv_asset FOREIGN KEY (inv_asset_id) REFERENCES inv_asset (id) ON DELETE RESTRICT
);

CREATE INDEX idx_dsa_inv_asset_id ON data_snapshot_asset (inv_asset_id);

CREATE TABLE plan (
    id                          BIGSERIAL PRIMARY KEY,
    public_id                   UUID        NOT NULL,
    data_snapshot_id            BIGINT      NOT NULL,
    goal_amount                 BIGINT      NOT NULL,
    horizon_months              SMALLINT    NOT NULL,
    funds_initial               BIGINT      NOT NULL,
    funds_monthly               BIGINT      NOT NULL,
    alloc_initial_invest_pct    SMALLINT    NOT NULL,
    alloc_initial_safe_pct      SMALLINT    NOT NULL,
    alloc_initial_other_pct     SMALLINT    NOT NULL,
    alloc_monthly_invest_pct    SMALLINT    NOT NULL,
    alloc_monthly_safe_pct      SMALLINT    NOT NULL,
    alloc_monthly_other_pct     SMALLINT    NOT NULL,
    focus_period_enum           CHAR(1)     NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_plan_public_id UNIQUE (public_id),
    CONSTRAINT fk_plan_data_snapshot FOREIGN KEY (data_snapshot_id) REFERENCES data_snapshot (id) ON DELETE RESTRICT,
    CONSTRAINT chk_plan_horizon_months CHECK (horizon_months BETWEEN 12 AND 120),
    CONSTRAINT chk_plan_focus_period CHECK (focus_period_enum IN ('M', 'Q', 'H'))
);

CREATE INDEX idx_plan_data_snapshot_id ON plan (data_snapshot_id);

CREATE TABLE inv_holding (
    plan_id     BIGINT   NOT NULL,
    asset_id    BIGINT   NOT NULL,
    weight_pct  SMALLINT NOT NULL,
    PRIMARY KEY (plan_id, asset_id),
    CONSTRAINT fk_inv_holding_plan FOREIGN KEY (plan_id) REFERENCES plan (id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_holding_asset FOREIGN KEY (asset_id) REFERENCES inv_asset (id) ON DELETE RESTRICT,
    CONSTRAINT chk_inv_holding_weight_pct CHECK (weight_pct BETWEEN 1 AND 100)
);

CREATE INDEX idx_inv_holding_asset_id ON inv_holding (asset_id);
