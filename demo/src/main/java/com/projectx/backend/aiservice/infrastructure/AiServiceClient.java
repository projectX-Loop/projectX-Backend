package com.projectx.backend.aiservice.infrastructure;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * ai-service(FastAPI, 성종현 레포) 내부 HTTP 계약(KAN-17) 클라이언트.
 *
 * <p>계산과 설명은 분리해서 부른다(KAN-4 결정) — {@code /calculate}가 실패해도 결과 화면 자체를 못 그리는 것과
 * {@code /rag/answer}·{@code /rag/ask}가 실패해도 결과 화면은 유지되는 것은 별개 실패 경로다.
 *
 * <p>{@code /rag/answer}·{@code /rag/ask}는 처리가 끝나면 항상 200을 주고 성패는 응답 본문의 {@code status}로
 * 구분한다(EXPLANATION_REJECTED·EXPLANATION_UNAVAILABLE 포함) — 그래서 이 클라이언트는 그 응답 바디를 있는 그대로
 * 돌려줄 뿐 상태값으로 예외를 던지지 않는다. 예외는 네트워크 실패·비정상 HTTP 상태(연결 자체가 안 되는 경우)에서만 던진다.
 */
@Slf4j
@Component
public class AiServiceClient {

	private final RestClient restClient;

	public AiServiceClient(RestClient aiServiceRestClient) {
		this.restClient = aiServiceRestClient;
	}

	/** Kan-9 §2 입력 dict → §5 결과 JSON. 422/503/그 외를 KAN-17 규약대로 구분한다. */
	public JsonNode calculate(JsonNode planInputs) {
		try {
			return restClient.post()
					.uri("/calculate")
					.contentType(MediaType.APPLICATION_JSON)
					.body(planInputs)
					.retrieve()
					.body(JsonNode.class);
		}
		catch (HttpStatusCodeException e) {
			int status = e.getStatusCode().value();
			if (status == 422) {
				log.warn("ai-service validation rejection: {}", e.getResponseBodyAsString());
				throw new BusinessException(ErrorCode.VALIDATION_ERROR);
			}
			if (status == 503) {
				throw new BusinessException(ErrorCode.ENGINE_UNAVAILABLE);
			}
			throw new BusinessException(ErrorCode.CALCULATION_FAILED);
		}
		catch (RestClientException e) {
			throw new BusinessException(ErrorCode.ENGINE_UNAVAILABLE);
		}
	}

	/** 시뮬레이션 결과(+ focus·goal_amount) → AI 설명. 항상 200 + status인 본문을 그대로 돌려준다. */
	public JsonNode explain(JsonNode payload) {
		return post("/rag/answer", payload, ErrorCode.EXPLANATION_UNAVAILABLE);
	}

	/** 시뮬레이션 결과 + question(+history) → 단발/멀티턴 답변. 항상 200 + status인 본문을 그대로 돌려준다. */
	public JsonNode ask(JsonNode payload) {
		return post("/rag/ask", payload, ErrorCode.ANSWER_UNAVAILABLE);
	}

	private JsonNode post(String uri, JsonNode payload, ErrorCode unavailableCode) {
		try {
			return restClient.post()
					.uri(uri)
					.contentType(MediaType.APPLICATION_JSON)
					.body(payload.toString())
					.retrieve()
					.body(JsonNode.class);
		}
		catch (RestClientResponseException e) {
			log.warn("ai-service {} returned HTTP {}: {}", uri, e.getStatusCode().value(),
					e.getResponseBodyAsString());
			throw new BusinessException(unavailableCode);
		}
		catch (RestClientException e) {
			// 정상 실패(REJECTED 등)는 200 + status로 오므로 여기까지 안 온다 — 연결 실패·타임아웃·잘못된 본문(422)만 해당.
			log.warn("ai-service {} request failed: {}", uri, e.getMessage());
			throw new BusinessException(unavailableCode);
		}
	}

}
