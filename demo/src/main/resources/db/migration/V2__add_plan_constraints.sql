ALTER TABLE data_snapshot
    ADD CONSTRAINT chk_data_snapshot_month_range CHECK (start_month <= latest_month);

ALTER TABLE data_snapshot_asset
    ADD CONSTRAINT chk_data_snapshot_asset_month_range CHECK (available_from_month <= available_to_month);

ALTER TABLE plan
    ADD CONSTRAINT chk_plan_goal_amount CHECK (goal_amount BETWEEN 1000000 AND 10000000000),
    ADD CONSTRAINT chk_plan_funds_initial CHECK (funds_initial >= 0),
    ADD CONSTRAINT chk_plan_funds_monthly CHECK (funds_monthly >= 0),
    ADD CONSTRAINT chk_plan_alloc_initial_invest CHECK (alloc_initial_invest_pct BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_plan_alloc_initial_safe CHECK (alloc_initial_safe_pct BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_plan_alloc_initial_other CHECK (alloc_initial_other_pct BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_plan_alloc_monthly_invest CHECK (alloc_monthly_invest_pct BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_plan_alloc_monthly_safe CHECK (alloc_monthly_safe_pct BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_plan_alloc_monthly_other CHECK (alloc_monthly_other_pct BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_plan_alloc_initial_sum CHECK (
        alloc_initial_invest_pct + alloc_initial_safe_pct + alloc_initial_other_pct = 100
    ),
    ADD CONSTRAINT chk_plan_alloc_monthly_sum CHECK (
        alloc_monthly_invest_pct + alloc_monthly_safe_pct + alloc_monthly_other_pct = 100
    );
