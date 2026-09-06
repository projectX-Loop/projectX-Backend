package com.projectx.backend.plan.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.projectx.backend.plan.api.SamplesResponse.Sample;
import com.projectx.backend.plan.application.SampleQueryService;

import static org.mockito.Mockito.mock;

class SamplesControllerTests {

	@Test
	void returnsSamplesAtPublicEndpoint() throws Exception {
		SampleQueryService service = mock(SampleQueryService.class);
		given(service.getSamples()).willReturn(new SamplesResponse(List.of(new Sample("P0", "예시", null))));
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SamplesController(service)).build();

		mockMvc.perform(get("/api/v1/samples"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.samples[0].id").value("P0"))
				.andExpect(jsonPath("$.success").doesNotExist());
	}

}
