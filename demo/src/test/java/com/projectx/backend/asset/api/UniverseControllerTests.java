package com.projectx.backend.asset.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.projectx.backend.asset.application.UniverseQueryService;
import com.projectx.backend.asset.api.UniverseResponse.Snapshot;
import com.projectx.backend.asset.api.UniverseResponse.UniverseAsset;
import com.projectx.backend.asset.api.UniverseResponse.Window;

import static org.mockito.Mockito.mock;

class UniverseControllerTests {

	@Test
	void returnsCurrentCatalogWithoutGroupOrSafeRate() throws Exception {
		UniverseQueryService service = mock(UniverseQueryService.class);
		given(service.getUniverse()).willReturn(new UniverseResponse(new Snapshot("2026-09-02", "sha256:current",
				new Window("2021-08", "2026-07", 60)),
				List.of(new UniverseAsset("KR_EQ", "국내 주식", "069500", "domestic"))));
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UniverseController(service)).build();

		mockMvc.perform(get("/api/v1/universe"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.snapshot.data_version").value("2026-09-02"))
				.andExpect(jsonPath("$.snapshot.window.months").value(60))
				.andExpect(jsonPath("$.assets[0].code").value("KR_EQ"))
				.andExpect(jsonPath("$.assets[0].group").doesNotExist())
				.andExpect(jsonPath("$.snapshot.safe_rate_annual_pct").doesNotExist());
	}

}
