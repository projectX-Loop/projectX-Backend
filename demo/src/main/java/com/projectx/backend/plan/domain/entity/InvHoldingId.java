package com.projectx.backend.plan.domain.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvHoldingId implements Serializable {

	@Column(name = "plan_id")
	private Long planId;

	@Column(name = "asset_id")
	private Long assetId;

	public InvHoldingId(Long planId, Long assetId) {
		this.planId = planId;
		this.assetId = assetId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof InvHoldingId that)) {
			return false;
		}
		return Objects.equals(planId, that.planId) && Objects.equals(assetId, that.assetId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(planId, assetId);
	}

}
