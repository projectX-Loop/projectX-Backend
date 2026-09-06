package com.projectx.backend.snapshot.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_version", nullable = false)
	private String dataVersion;

	@Column(name = "data_hash", nullable = false, unique = true)
	private String dataHash;

	@Column(name = "start_month", nullable = false)
	private LocalDate startMonth;

	@Column(name = "latest_month", nullable = false)
	private LocalDate latestMonth;

	@Column(name = "safe_rate_annual_pct", nullable = false)
	private BigDecimal safeRateAnnualPct;

	@Column(name = "frozen", nullable = false)
	private boolean frozen;

	@Column(name = "is_current", nullable = false)
	private boolean isCurrent;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	public static DataSnapshot create(String dataVersion, String dataHash, LocalDate startMonth,
			LocalDate latestMonth, BigDecimal safeRateAnnualPct) {
		DataSnapshot dataSnapshot = new DataSnapshot();
		dataSnapshot.dataVersion = dataVersion;
		dataSnapshot.dataHash = dataHash;
		dataSnapshot.startMonth = startMonth;
		dataSnapshot.latestMonth = latestMonth;
		dataSnapshot.safeRateAnnualPct = safeRateAnnualPct;
		dataSnapshot.frozen = false;
		dataSnapshot.isCurrent = false;
		return dataSnapshot;
	}

	public void freeze() {
		this.frozen = true;
	}

	public void markAsCurrent() {
		this.isCurrent = true;
	}

	public void unmarkAsCurrent() {
		this.isCurrent = false;
	}

}
