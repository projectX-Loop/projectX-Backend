package com.projectx.backend.plan.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inv_holding")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvHolding {

	private static final short MIN_WEIGHT_PCT = 1;
	private static final short MAX_WEIGHT_PCT = 100;

	@EmbeddedId
	private InvHoldingId id;

	@MapsId("planId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(name = "weight_pct", nullable = false)
	private short weightPct;

	public static InvHolding create(Plan plan, Long assetId, short weightPct) {
		if (weightPct < MIN_WEIGHT_PCT || weightPct > MAX_WEIGHT_PCT) {
			throw new IllegalArgumentException(
					"weightPct must be between " + MIN_WEIGHT_PCT + " and " + MAX_WEIGHT_PCT);
		}

		InvHolding invHolding = new InvHolding();
		invHolding.id = new InvHoldingId(plan.getId(), assetId);
		invHolding.plan = plan;
		invHolding.weightPct = weightPct;
		return invHolding;
	}

}
