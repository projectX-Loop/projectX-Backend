package com.projectx.backend.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CloudFrontOriginSecretFilterTests {

	private final CloudFrontOriginSecretFilter filter = new CloudFrontOriginSecretFilter("origin-secret");

	@Test
	void rejectsApiRequestWithoutCloudFrontHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
	}

	@Test
	void allowsApiRequestWithMatchingCloudFrontHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
		request.addHeader("X-ProjectX-Origin-Verify", "origin-secret");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		filter.doFilter(request, response, filterChain);

		assertThat(filterChain.getRequest()).isSameAs(request);
	}

	@Test
	void requiresOriginSecretInProd() {
		assertThatThrownBy(() -> new CloudFrontOriginSecretFilter(" "))
				.isInstanceOf(IllegalStateException.class);
	}

}
