package com.projectx.backend.aiservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.ObjectMapper;

class AiServiceClientConfigTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void sendsRagPayloadBodyWithConfiguredRequestFactory() throws Exception {
		AtomicReference<String> requestBody = new AtomicReference<>();
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/rag/answer", exchange -> {
			requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] response = "{\"status\":\"OK\"}".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});
		server.start();

		try {
			AiServiceClientConfig config = new AiServiceClientConfig();
			AiServiceProperties properties = new AiServiceProperties(
					"http://localhost:" + server.getAddress().getPort(), Duration.ofSeconds(3));
			AiServiceClient client = new AiServiceClient(config.aiServiceRestClient(properties));
			var payload = objectMapper.createObjectNode().put("focus", "FIRE");

			client.explain(payload);

			assertThat(requestBody.get()).isEqualTo("{\"focus\":\"FIRE\"}");
		}
		finally {
			server.stop(0);
		}
	}
}
