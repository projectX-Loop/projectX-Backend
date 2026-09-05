package com.projectx.backend.plan.infrastructure;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-service")
public record AiServiceProperties(String baseUrl, Duration calculateTimeout) {

}
