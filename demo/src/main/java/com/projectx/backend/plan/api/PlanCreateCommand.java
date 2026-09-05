package com.projectx.backend.plan.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlanCreateCommand(
		PlanCreateRequest.Goal goal,
		PlanCreateRequest.Funds funds,
		PlanCreateRequest.Allocation alloc,
		PlanCreateRequest.Portfolio portfolio,
		PlanCreateRequest.Rebalancing rebalancing) {

	@JsonProperty("goal")
	public PlanCreateRequest.Goal goal() {
		return goal;
	}

}
