package com.projectx.backend.plan.api;

public record PlanCreateCommand(
		PlanCreateRequest.Goal goal,
		PlanCreateRequest.Funds funds,
		PlanCreateRequest.Allocation alloc,
		PlanCreateRequest.Portfolio portfolio,
		PlanCreateRequest.Rebalancing rebalancing) {
}
