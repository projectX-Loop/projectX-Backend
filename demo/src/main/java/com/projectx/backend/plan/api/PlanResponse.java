package com.projectx.backend.plan.api;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.projectx.backend.plan.domain.entity.Plan;

public record PlanResponse(PlanInfo plan, @JsonRawValue String calculation) {

	public static PlanResponse from(Plan plan, PlanCreateCommand command, JsonNode calculation) {
		PlanCreateRequest inputs = new PlanCreateRequest(command.goal(), command.funds(), command.alloc(),
				command.portfolio(), command.rebalancing());
		return new PlanResponse(new PlanInfo(plan.getPublicId(), plan.getDataSnapshotId(), plan.getCreatedAt(), inputs),
				calculation.toString());
	}

	public record PlanInfo(@JsonProperty("public_id") UUID publicId,
			@JsonProperty("data_snapshot_id") Long dataSnapshotId,
			@JsonProperty("created_at") OffsetDateTime createdAt,
			PlanCreateRequest inputs) {
	}

}
