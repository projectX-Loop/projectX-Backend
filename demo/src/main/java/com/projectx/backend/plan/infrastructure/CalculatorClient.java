package com.projectx.backend.plan.infrastructure;

import java.net.http.HttpClient;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectx.backend.global.api.ValidationError;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.plan.api.PlanCreateCommand;

@Component
public class CalculatorClient {

	private final RestClient restClient;

	public CalculatorClient(AiServiceProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(properties.calculateTimeout())
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.calculateTimeout());
		this.restClient = RestClient.builder().baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.build();
	}

	public JsonNode calculate(PlanCreateCommand command) {
		try {
			JsonNode response = restClient.post()
					.uri("/calculate")
					.contentType(MediaType.APPLICATION_JSON)
					.body(command)
					.retrieve()
					.body(JsonNode.class);
			if (response == null) {
				throw new BusinessException(ErrorCode.CALCULATOR_RESPONSE_INVALID);
			}
			return response;
		} catch (BusinessException exception) {
			throw exception;
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST,
						List.of(new ValidationError("calculator", exception.getResponseBodyAsString())));
			}
			throw new BusinessException(ErrorCode.CALCULATOR_UNAVAILABLE);
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.CALCULATOR_UNAVAILABLE);
		}
	}

}
