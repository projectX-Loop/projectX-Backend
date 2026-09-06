package com.projectx.backend.plan.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectx.backend.plan.api.PlanCreateRequest.Allocation;
import com.projectx.backend.plan.api.PlanCreateRequest.AllocationItem;
import com.projectx.backend.plan.api.PlanCreateRequest.Funds;
import com.projectx.backend.plan.api.PlanCreateRequest.Goal;
import com.projectx.backend.plan.api.PlanCreateRequest.Portfolio;
import com.projectx.backend.plan.api.PlanCreateRequest.Rebalancing;
import com.projectx.backend.plan.domain.entity.Plan;

import static org.mockito.Mockito.mock;

class PlanResponseTests {

	@Test
	void includesStoredPlanMetadataAndOriginalInputs() throws Exception {
		Plan plan = mock(Plan.class);
		UUID publicId = UUID.randomUUID();
		OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-06T00:00:00Z");
		given(plan.getPublicId()).willReturn(publicId);
		given(plan.getDataSnapshotId()).willReturn(9L);
		given(plan.getCreatedAt()).willReturn(createdAt);
		PlanCreateCommand command = new PlanCreateCommand(new Goal(50_000_000L, 60), new Funds(10_000_000L, 600_000L),
				new Allocation(new AllocationItem(70, 30, 0), new AllocationItem(50, 40, 10)),
				new Portfolio(List.of()), new Rebalancing("Q"));

		PlanResponse response = PlanResponse.from(plan, command,
				new ObjectMapper().readTree("{\"status\":\"OK\"}"));

		assertThat(response.plan().publicId()).isEqualTo(publicId);
		assertThat(response.plan().dataSnapshotId()).isEqualTo(9L);
		assertThat(response.plan().createdAt()).isEqualTo(createdAt);
		assertThat(response.plan().inputs().goal()).isEqualTo(command.goal());
		assertThat(response.calculation()).isEqualTo("{\"status\":\"OK\"}");
	}

}
