package com.projectx.backend.plan.api;

import java.util.List;

public record SamplesResponse(List<Sample> samples) {

	public record Sample(String id, String label, PlanCreateRequest inputs) {
	}

}
