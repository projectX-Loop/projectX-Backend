UPDATE data_snapshot
SET is_current = FALSE
WHERE is_current = TRUE;

INSERT INTO data_snapshot (data_version, data_hash, start_month, latest_month, safe_rate_annual_pct, frozen, is_current)
VALUES ('2026-09-02', 'sha256:fa84100c67a0589aaa3cdb88a13138b87dba6417403f56608680896dbfddb90d',
        DATE '2009-08-01', DATE '2026-07-01', 3.15, FALSE, TRUE)
ON CONFLICT (data_hash) DO UPDATE
SET data_version = EXCLUDED.data_version,
    start_month = EXCLUDED.start_month,
    latest_month = EXCLUDED.latest_month,
    safe_rate_annual_pct = EXCLUDED.safe_rate_annual_pct,
    frozen = EXCLUDED.frozen,
    is_current = EXCLUDED.is_current;

INSERT INTO inv_asset (code, instrument, display_name, currency_code, tax_class)
VALUES
    ('KR_EQ', '069500', 'KODEX 200', 'KRW', 'domestic_equity'),
    ('US_EQ', 'SPY', '해외 주식 (SPY × USD/KRW)', 'USD', 'foreign_listed'),
    ('KR_BOND', '114260', 'KODEX 국고채3년', 'KRW', 'domestic_listed_other'),
    ('US_EQ_KR', '360750', 'TIGER 미국S&P500', 'KRW', 'domestic_listed_other')
ON CONFLICT (code) DO UPDATE
SET instrument = EXCLUDED.instrument,
    display_name = EXCLUDED.display_name,
    currency_code = EXCLUDED.currency_code,
    tax_class = EXCLUDED.tax_class;

WITH asset_windows (code, available_from_month, available_to_month) AS (
    VALUES
        ('KR_EQ', DATE '2009-02-01', DATE '2026-07-01'),
        ('US_EQ', DATE '2009-02-01', DATE '2026-07-01'),
        ('KR_BOND', DATE '2009-08-01', DATE '2026-07-01'),
        ('US_EQ_KR', DATE '2020-09-01', DATE '2026-07-01')
)
INSERT INTO data_snapshot_asset (data_snapshot_id, inv_asset_id, available_from_month, available_to_month)
SELECT data_snapshot.id, inv_asset.id, asset_windows.available_from_month, asset_windows.available_to_month
FROM asset_windows
JOIN inv_asset ON inv_asset.code = asset_windows.code
JOIN data_snapshot ON data_snapshot.data_hash = 'sha256:fa84100c67a0589aaa3cdb88a13138b87dba6417403f56608680896dbfddb90d'
ON CONFLICT (data_snapshot_id, inv_asset_id) DO UPDATE
SET available_from_month = EXCLUDED.available_from_month,
    available_to_month = EXCLUDED.available_to_month;
