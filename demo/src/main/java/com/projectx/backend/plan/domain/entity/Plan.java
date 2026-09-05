package com.projectx.backend.plan.domain.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

	private static final short MIN_HORIZON_MONTHS = 12;
	private static final short MAX_HORIZON_MONTHS = 120;
	private static final short TOTAL_ALLOC_PCT = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "public_id", nullable = false, unique = true)
	private UUID publicId;

	@Column(name = "data_snapshot_id", nullable = false)
	private Long dataSnapshotId;

	@Column(name = "goal_amount", nullable = false)
	private long goalAmount;

	@Column(name = "horizon_months", nullable = false)
	private short horizonMonths;

	@Column(name = "funds_initial", nullable = false)
	private long fundsInitial;

	@Column(name = "funds_monthly", nullable = false)
	private long fundsMonthly;

	@Column(name = "alloc_initial_invest_pct", nullable = false)
	private short allocInitialInvestPct;

	@Column(name = "alloc_initial_safe_pct", nullable = false)
	private short allocInitialSafePct;

	@Column(name = "alloc_initial_other_pct", nullable = false)
	private short allocInitialOtherPct;

	@Column(name = "alloc_monthly_invest_pct", nullable = false)
	private short allocMonthlyInvestPct;

	@Column(name = "alloc_monthly_safe_pct", nullable = false)
	private short allocMonthlySafePct;

	@Column(name = "alloc_monthly_other_pct", nullable = false)
	private short allocMonthlyOtherPct;

	@Convert(converter = FocusPeriodConverter.class)
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "focus_period_enum", nullable = false, columnDefinition = "char(1)")
	private FocusPeriod focusPeriod;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	public static Plan create(Long dataSnapshotId, long goalAmount, short horizonMonths, long fundsInitial,
			long fundsMonthly, short allocInitialInvestPct, short allocInitialSafePct, short allocInitialOtherPct,
			short allocMonthlyInvestPct, short allocMonthlySafePct, short allocMonthlyOtherPct,
			FocusPeriod focusPeriod) {
		if (horizonMonths < MIN_HORIZON_MONTHS || horizonMonths > MAX_HORIZON_MONTHS) {
			throw new IllegalArgumentException(
					"horizonMonths must be between " + MIN_HORIZON_MONTHS + " and " + MAX_HORIZON_MONTHS);
		}
		if (allocInitialInvestPct + allocInitialSafePct + allocInitialOtherPct != TOTAL_ALLOC_PCT) {
			throw new IllegalArgumentException("alloc_initial_*_pct must sum to " + TOTAL_ALLOC_PCT);
		}
		if (allocMonthlyInvestPct + allocMonthlySafePct + allocMonthlyOtherPct != TOTAL_ALLOC_PCT) {
			throw new IllegalArgumentException("alloc_monthly_*_pct must sum to " + TOTAL_ALLOC_PCT);
		}

		Plan plan = new Plan();
		plan.publicId = UUID.randomUUID();
		plan.dataSnapshotId = dataSnapshotId;
		plan.goalAmount = goalAmount;
		plan.horizonMonths = horizonMonths;
		plan.fundsInitial = fundsInitial;
		plan.fundsMonthly = fundsMonthly;
		plan.allocInitialInvestPct = allocInitialInvestPct;
		plan.allocInitialSafePct = allocInitialSafePct;
		plan.allocInitialOtherPct = allocInitialOtherPct;
		plan.allocMonthlyInvestPct = allocMonthlyInvestPct;
		plan.allocMonthlySafePct = allocMonthlySafePct;
		plan.allocMonthlyOtherPct = allocMonthlyOtherPct;
		plan.focusPeriod = focusPeriod;
		return plan;
	}

}
