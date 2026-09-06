package com.projectx.backend.plan.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.projectx.backend.plan.api.SamplesResponse;

class SampleQueryServiceTests {

	@Test
	void returnsFixedP0TemplateThatIsValidPlanInput() {
		SamplesResponse response = new SampleQueryService().getSamples();

		assertThat(response.samples()).hasSize(1);
		SamplesResponse.Sample sample = response.samples().get(0);
		assertThat(sample.id()).isEqualTo("P0");
		assertThat(sample.inputs().goal().amount()).isEqualTo(50_000_000L);
		assertThat(sample.inputs().portfolio().assets()).extracting(asset -> asset.code())
				.containsExactly("KR_EQ", "US_EQ", "KR_BOND");
		assertThat(sample.inputs().portfolio().assets()).extracting(asset -> asset.weight())
				.containsExactly(40, 40, 20);
	}

}
