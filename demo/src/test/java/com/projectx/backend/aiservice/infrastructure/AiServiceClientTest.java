package com.projectx.backend.aiservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** ai-service KAN-17 상태 코드 매핑이 실제로 맞는지 — Mock HTTP 서버로 확인(진짜 ai-service 없이). */
class AiServiceClientTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockRestServiceServer server;
	private AiServiceClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ai-service.test");
		server = MockRestServiceServer.bindTo(builder).build();
		client = new AiServiceClient(builder.build());
	}

	@Test
	void calculateMapsUnprocessableEntityToValidationError() {
		server.expect(requestTo("http://ai-service.test/calculate"))
				.andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT).contentType(MediaType.APPLICATION_JSON)
						.body("{\"code\":\"VALIDATION_ERROR\"}"));

		assertThatThrownBy(() -> client.calculate(objectMapper.createObjectNode())).isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
	}

	@Test
	void calculateMapsServiceUnavailableToEngineUnavailable() {
		server.expect(requestTo("http://ai-service.test/calculate")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

		assertThatThrownBy(() -> client.calculate(objectMapper.createObjectNode())).isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ENGINE_UNAVAILABLE));
	}

	@Test
	void explainReturnsBodyAsIsOnOkStatus() {
		server.expect(requestTo("http://ai-service.test/rag/answer"))
				.andExpect(content().json("{\"meta\":{\"data_hash\":\"sha256:test\"},\"focus\":\"Q\"}"))
				.andRespond(withSuccess("{\"status\":\"OK\"}", MediaType.APPLICATION_JSON));

		var payload = objectMapper.createObjectNode();
		payload.putObject("meta").put("data_hash", "sha256:test");
		payload.put("focus", "Q");
		JsonNode result = client.explain(payload);

		assertThat(result.get("status").asString()).isEqualTo("OK");
	}

	@Test
	void explainReturnsBodyAsIsOnRejectedStatus() {
		// KAN-17: 가드레일 반려도 200 + status로 온다 — 예외가 아니라 그대로 통과시켜야 한다.
		server.expect(requestTo("http://ai-service.test/rag/answer"))
				.andRespond(withSuccess("{\"status\":\"EXPLANATION_REJECTED\",\"explanation\":null}", MediaType.APPLICATION_JSON));

		JsonNode result = client.explain(objectMapper.createObjectNode());

		assertThat(result.get("status").asString()).isEqualTo("EXPLANATION_REJECTED");
	}

	@Test
	void explainThrowsUnavailableWhenAiServiceUnreachable() {
		AiServiceClient unreachable = new AiServiceClient(RestClient.builder().baseUrl("http://localhost:1").build());

		assertThatThrownBy(() -> unreachable.explain(objectMapper.createObjectNode())).isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.EXPLANATION_UNAVAILABLE));
	}

}
