package com.projectx.backend.plan.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.api.PlanCreateCommand;
import com.projectx.backend.plan.api.PlanCreateRequest.Allocation;
import com.projectx.backend.plan.api.PlanCreateRequest.AllocationItem;
import com.projectx.backend.plan.api.PlanCreateRequest.AssetWeight;
import com.projectx.backend.plan.api.PlanCreateRequest.Funds;
import com.projectx.backend.plan.api.PlanCreateRequest.Goal;
import com.projectx.backend.plan.api.PlanCreateRequest.Portfolio;
import com.projectx.backend.plan.api.PlanCreateRequest.Rebalancing;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class CalculatorClientTests {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AtomicReference<String> requestMethod = new AtomicReference<>();
	private final AtomicReference<String> requestPath = new AtomicReference<>();
	private final AtomicReference<String> requestBody = new AtomicReference<>();

	private HttpServer server;

	@BeforeEach
	void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.start();
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
	}

	@Test
	void sendsPlanInputsToCalculateEndpoint() throws Exception {
		server.createContext("/calculate", exchange -> {
			capture(exchange);
			respond(exchange, 200, "{\"status\":\"OK\",\"meta\":{\"data_hash\":\"sha256:mvp-2021-08-2026-07-v1\"}}");
		});
		CalculatorClient client = client();

		JsonNode response = client.calculate(command());

		assertThat(requestMethod.get()).isEqualTo("POST");
		assertThat(requestPath.get()).isEqualTo("/calculate");
		JsonNode request = objectMapper.readTree(requestBody.get());
		assertThat(request.path("goal").path("amount").asLong()).isEqualTo(50_000_000L);
		assertThat(request.path("goal").path("horizon_months").asInt()).isEqualTo(60);
		assertThat(request.path("portfolio").path("assets").get(0).path("code").asText()).isEqualTo("KR_EQ");
		assertThat(response.path("meta").path("data_hash").asText()).isEqualTo("sha256:mvp-2021-08-2026-07-v1");
	}

	@Test
	void mapsUnprocessableEntityToInvalidRequest() {
		server.createContext("/calculate", exchange -> respond(exchange, 422, "invalid portfolio"));
		CalculatorClient client = client();

		assertThatThrownBy(() -> client.calculate(command()))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> {
					BusinessException businessException = (BusinessException) exception;
					assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
					assertThat(businessException.getErrors()).singleElement()
							.satisfies(error -> assertThat(error.field()).isEqualTo("calculator"));
				});
	}

	@Test
	void rejectsEmptyCalculatorResponse() {
		server.createContext("/calculate", exchange -> exchange.sendResponseHeaders(200, -1));
		CalculatorClient client = client();

		assertThatThrownBy(() -> client.calculate(command()))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
						.isEqualTo(ErrorCode.CALCULATOR_RESPONSE_INVALID));
	}

	@Test
	void rejectsMalformedCalculatorResponse() {
		server.createContext("/calculate", exchange -> respond(exchange, 200, "not-json"));
		CalculatorClient client = client();

		assertThatThrownBy(() -> client.calculate(command()))
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
						.isEqualTo(ErrorCode.CALCULATOR_RESPONSE_INVALID));
	}

	private CalculatorClient client() {
		return new CalculatorClient(new AiServiceProperties("http://localhost:" + server.getAddress().getPort(),
				Duration.ofSeconds(1)));
	}

	private PlanCreateCommand command() {
		return new PlanCreateCommand(new Goal(50_000_000L, 60), new Funds(10_000_000L, 600_000L),
				new Allocation(new AllocationItem(60, 30, 10), new AllocationItem(50, 40, 10)),
				new Portfolio(List.of(new AssetWeight("KR_EQ", 100))), new Rebalancing("Q"));
	}

	private void capture(HttpExchange exchange) throws IOException {
		requestMethod.set(exchange.getRequestMethod());
		requestPath.set(exchange.getRequestURI().getPath());
		requestBody.set(new String(exchange.getRequestBody().readAllBytes()));
	}

	private void respond(HttpExchange exchange, int status, String body) throws IOException {
		byte[] response = body.getBytes();
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();
	}

}
