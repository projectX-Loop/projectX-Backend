ALTER TABLE data_snapshot
    ADD COLUMN safe_rate_annual_pct NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    ADD CONSTRAINT chk_data_snapshot_safe_rate CHECK (safe_rate_annual_pct >= 0);

CREATE TABLE data_snapshot_asset_return (
    data_snapshot_id   BIGINT       NOT NULL,
    inv_asset_id       BIGINT       NOT NULL,
    return_month       DATE         NOT NULL,
    monthly_return_pct NUMERIC(7,4) NOT NULL,
    PRIMARY KEY (data_snapshot_id, inv_asset_id, return_month),
    CONSTRAINT fk_dsar_snapshot_asset FOREIGN KEY (data_snapshot_id, inv_asset_id)
        REFERENCES data_snapshot_asset (data_snapshot_id, inv_asset_id) ON DELETE CASCADE
);

UPDATE data_snapshot
SET is_current = FALSE
WHERE is_current = TRUE;

INSERT INTO data_snapshot (data_version, data_hash, start_month, latest_month, safe_rate_annual_pct, frozen, is_current)
VALUES ('mvp-2026-07', 'sha256:mvp-2021-08-2026-07-v1', DATE '2021-08-01', DATE '2026-07-01', 3.00, TRUE, TRUE)
ON CONFLICT (data_hash) DO UPDATE
SET data_version = EXCLUDED.data_version,
    start_month = EXCLUDED.start_month,
    latest_month = EXCLUDED.latest_month,
    safe_rate_annual_pct = EXCLUDED.safe_rate_annual_pct,
    frozen = EXCLUDED.frozen,
    is_current = EXCLUDED.is_current;

INSERT INTO inv_asset (code, instrument, display_name, currency_code, tax_class)
VALUES
    ('KR_EQ', '069500', 'KODEX 200', 'KRW', 'domestic'),
    ('US_EQ', 'SPY', 'SPDR S&P 500 ETF', 'USD', 'foreign'),
    ('KR_BOND', '148070', 'KODEX Government Bond 10Y', 'KRW', 'domestic'),
    ('US_EQ_KR', '360750', 'TIGER US S&P 500', 'KRW', 'domestic')
ON CONFLICT (code) DO UPDATE
SET instrument = EXCLUDED.instrument,
    display_name = EXCLUDED.display_name,
    currency_code = EXCLUDED.currency_code,
    tax_class = EXCLUDED.tax_class;

INSERT INTO data_snapshot_asset (data_snapshot_id, inv_asset_id, available_from_month, available_to_month)
SELECT data_snapshot.id, inv_asset.id, DATE '2021-08-01', DATE '2026-07-01'
FROM data_snapshot
JOIN inv_asset ON inv_asset.code IN ('KR_EQ', 'US_EQ', 'KR_BOND', 'US_EQ_KR')
WHERE data_snapshot.data_hash = 'sha256:mvp-2021-08-2026-07-v1'
ON CONFLICT (data_snapshot_id, inv_asset_id) DO UPDATE
SET available_from_month = EXCLUDED.available_from_month,
    available_to_month = EXCLUDED.available_to_month;

WITH return_patterns (code, monthly_returns) AS (
    VALUES
        ('KR_EQ', ARRAY[2.1000, -1.4000, 3.0000, 0.8000, -2.2000, 1.7000, 2.5000, -0.9000, 1.2000, -3.1000, 2.8000, 1.5000]::NUMERIC[]),
        ('US_EQ', ARRAY[1.8000, -2.0000, 2.6000, 1.1000, -1.8000, 2.2000, 1.9000, -1.1000, 0.8000, -2.5000, 2.4000, 1.7000]::NUMERIC[]),
        ('KR_BOND', ARRAY[0.3000, 0.1000, 0.4000, 0.2000, -0.1000, 0.3000, 0.2000, 0.0000, 0.3000, -0.2000, 0.4000, 0.2000]::NUMERIC[]),
        ('US_EQ_KR', ARRAY[2.0000, -1.7000, 3.1000, 0.9000, -2.1000, 2.4000, 2.2000, -0.8000, 1.0000, -2.8000, 2.7000, 1.9000]::NUMERIC[])
), scenario_returns AS (
    SELECT return_patterns.code,
        (DATE '2021-08-01' + (month_index || ' months')::INTERVAL)::DATE AS return_month,
        return_patterns.monthly_returns[(month_index % 12) + 1] AS monthly_return_pct
    FROM return_patterns
    CROSS JOIN generate_series(0, 59) AS months(month_index)
)
INSERT INTO data_snapshot_asset_return (data_snapshot_id, inv_asset_id, return_month, monthly_return_pct)
SELECT data_snapshot.id, inv_asset.id, scenario_returns.return_month, scenario_returns.monthly_return_pct
FROM scenario_returns
JOIN inv_asset ON inv_asset.code = scenario_returns.code
JOIN data_snapshot ON data_snapshot.data_hash = 'sha256:mvp-2021-08-2026-07-v1'
ON CONFLICT (data_snapshot_id, inv_asset_id, return_month) DO UPDATE
SET monthly_return_pct = EXCLUDED.monthly_return_pct;
