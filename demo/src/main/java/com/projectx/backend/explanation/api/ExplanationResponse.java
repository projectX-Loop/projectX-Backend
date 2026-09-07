package com.projectx.backend.explanation.api;

import tools.jackson.databind.JsonNode;

/**
 * {@code POST /api/v1/plans/{publicId}/explanation} 응답 — ai-service·프론트가 이미 합의한 KAN-4 계약
 * (explainer/public_api.py {@code ExplanationResponse})과 필드가 같아야 한다. 항상 200이고 성패는 status로 구분한다.
 * 이 엔드포인트는 의도적으로 공용 {@code ApiResponse<T>} 래퍼를 쓰지 않는다 — 프론트 타입이 이미 이 모양(OpenAPI 생성)을
 * 그대로 기대하고 있어서, 래핑하면 계약이 깨진다.
 */
public record ExplanationResponse(String status, JsonNode explanation, String message) {
}
