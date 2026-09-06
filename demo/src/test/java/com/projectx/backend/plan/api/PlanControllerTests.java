package com.projectx.backend.plan.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectx.backend.plan.application.PlanCreateService;
import com.projectx.backend.plan.application.PlanQueryService;

import static org.mockito.Mockito.mock;

class PlanControllerTests {

	@Test
	void returnsCommonPlanResponseWithoutSuccessWrapper() throws Exception {
		PlanCreateService service = mock(PlanCreateService.class);
		UUID publicId = UUID.randomUUID();
		PlanResponse response = new PlanResponse(new PlanResponse.PlanInfo(publicId, 9L, null, null),
				"{\"status\":\"OK\"}");
		given(service.create(any())).willReturn(response);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlanController(service, mock(PlanQueryService.class))).build();

		mockMvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content(validRequest()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.plan.public_id").value(publicId.toString()))
				.andExpect(jsonPath("$.calculation.status").value("OK"))
				.andExpect(jsonPath("$.success").doesNotExist());
	}

	@Test
	void returnsRecalculatedPlanAtPublicIdEndpoint() throws Exception {
		PlanCreateService createService = mock(PlanCreateService.class);
		PlanQueryService queryService = mock(PlanQueryService.class);
		UUID publicId = UUID.randomUUID();
		given(queryService.get(publicId)).willReturn(new PlanResponse(new PlanResponse.PlanInfo(publicId, 9L, null, null),
				"{\"status\":\"OK\"}"));
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlanController(createService, queryService)).build();

		mockMvc.perform(get("/api/v1/plans/{publicId}", publicId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.plan.public_id").value(publicId.toString()))
				.andExpect(jsonPath("$.calculation.status").value("OK"));
	}

	private String validRequest() {
		return """
				{
				  "goal":{"amount":50000000,"horizon_months":60},
				  "funds":{"initial":10000000,"monthly":600000},
				  "alloc":{"initial":{"invest":70,"safe":30,"other":0},"monthly":{"invest":50,"safe":40,"other":10}},
				  "portfolio":{"assets":[{"code":"KR_EQ","weight":100}]},
				  "rebalancing":{"focus":"Q"}
				}
				""";
	}

}
