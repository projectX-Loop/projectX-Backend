package com.projectx.backend.plan.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectx.backend.plan.api.PlanCreateRequest;
import com.projectx.backend.plan.api.PlanCreateRequest.Allocation;
import com.projectx.backend.plan.api.PlanCreateRequest.AllocationItem;
import com.projectx.backend.plan.api.PlanCreateRequest.AssetWeight;
import com.projectx.backend.plan.api.PlanCreateRequest.Funds;
import com.projectx.backend.plan.api.PlanCreateRequest.Goal;
import com.projectx.backend.plan.api.PlanCreateRequest.Portfolio;
import com.projectx.backend.plan.api.PlanCreateRequest.Rebalancing;
import com.projectx.backend.plan.api.SamplesResponse;
import com.projectx.backend.plan.api.SamplesResponse.Sample;

@Service
public class SampleQueryService {

	@Transactional(readOnly = true)
	public SamplesResponse getSamples() {
		return new SamplesResponse(List.of(new Sample("P0", "5년 뒤 5,000만원 · 초기 1,000만원 · 월 60만원", p0Inputs())));
	}

	private PlanCreateRequest p0Inputs() {
		return new PlanCreateRequest(new Goal(50_000_000L, 60), new Funds(10_000_000L, 600_000L),
				new Allocation(new AllocationItem(70, 30, 0), new AllocationItem(50, 40, 10)),
				new Portfolio(List.of(new AssetWeight("KR_EQ", 40), new AssetWeight("US_EQ", 40),
						new AssetWeight("KR_BOND", 20))), new Rebalancing("Q"));
	}

}
