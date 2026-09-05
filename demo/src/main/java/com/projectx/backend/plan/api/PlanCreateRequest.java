package com.projectx.backend.plan.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlanCreateRequest(
		@NotNull @Valid Goal goal,
		@NotNull @Valid Funds funds,
		@NotNull @Valid Allocation alloc,
		@NotNull @Valid Portfolio portfolio,
		@NotNull @Valid Rebalancing rebalancing) {

	public PlanCreateCommand toCommand() {
		return new PlanCreateCommand(goal, funds, alloc, portfolio, rebalancing);
	}

	public record Goal(
			@NotNull Long amount,
			@JsonProperty("horizon_months") @NotNull Integer horizonMonths) {
	}

	public record Funds(
			@NotNull Long initial,
			@NotNull Long monthly) {
	}

	public record Allocation(
			@NotNull @Valid AllocationItem initial,
			@NotNull @Valid AllocationItem monthly) {
	}

	public record AllocationItem(
			@NotNull Integer invest,
			@NotNull Integer safe,
			@NotNull Integer other) {
	}

	public record Portfolio(@NotNull List<@Valid AssetWeight> assets) {
	}

	public record AssetWeight(@NotBlank String code, @NotNull Integer weight) {
	}

	public record Rebalancing(@NotBlank String focus) {
	}

}
