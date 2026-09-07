package com.projectx.backend.aiservice.infrastructure;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * {@code AiServiceProperties}는 {@code DemoApplication}의 {@code @ConfigurationPropertiesScan}이
 * 자동 등록한다(develop merge, 9/6) — 여기서 {@code @EnableConfigurationProperties}로 다시 등록하면
 * 같은 클래스가 중복 등록될 수 있어 뺐다.
 *
 * <p>Spring Boot 4.1.1엔 {@code RestClient.Builder} 자동 구성 빈이 없다(실제 컨텍스트 기동으로 확인 —
 * {@code spring-boot-autoconfigure} 4.1.1 jar에 관련 자동구성 클래스 자체가 없음). 그래서 주입받지 않고
 * {@code RestClient.builder()}를 직접 호출한다 — `plan.infrastructure.CalculatorClient`(도윤)도 같은
 * 이유로 같은 방식을 쓴다.
 *
 * <p>{@code RestClient}도 기본값은 타임아웃이 없다(요청이 끝날 때까지 무한정 기다림) — ai-service가
 * 멈추면 이 호출을 문 스레드가 영원히 붙잡힌다. {@code JdkClientHttpRequestFactory}로 연결·읽기 타임아웃을
 * 명시적으로 건다({@code ai-service.timeout}, 기본 90초 — ai-service README의 "/rag/answer 생성 20~60초 →
 * Spring 타임아웃 90초" 요구치와 맞춘다).
 */
@Configuration
public class AiServiceClientConfig {

	@Bean
	public RestClient aiServiceRestClient(AiServiceProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(properties.timeout())
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.timeout());

		return RestClient.builder()
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.build();
	}

}
