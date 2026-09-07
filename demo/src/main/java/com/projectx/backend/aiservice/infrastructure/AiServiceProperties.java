package com.projectx.backend.aiservice.infrastructure;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ai-service(FastAPI, 성종현) 연결 설정. {@code ai-service.base-url}은 환경변수 {@code AI_SERVICE_BASE_URL}로 덮는다.
 *
 * <p>{@code timeout}은 {@code /rag/answer}·{@code /rag/ask} 기준(ai-service 실측 20~60초, 문서상 요구치 90초)으로
 * 잡는다 — {@code /calculate}(정상 ~10ms)는 이 값보다 훨씬 빨리 끝나므로 실패 시에만 더 오래 기다리게 될 뿐,
 * 정상 경로엔 영향이 없다. 커넥션·읽기 둘 다 이 값을 쓴다(연결 자체가 안 되는 경우와 응답이 늦는 경우를 구분할
 * 필요가 없어서 — 어느 쪽이든 결과는 ENGINE_UNAVAILABLE/EXPLANATION_UNAVAILABLE로 같다).
 */
@ConfigurationProperties(prefix = "ai-service")
public record AiServiceProperties(String baseUrl, Duration timeout) {
}
