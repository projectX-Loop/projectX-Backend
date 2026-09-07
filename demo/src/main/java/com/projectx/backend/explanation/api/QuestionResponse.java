package com.projectx.backend.explanation.api;

import tools.jackson.databind.JsonNode;

/** {@code POST /api/v1/plans/{publicId}/questions} 응답 — ExplanationResponse와 같은 원칙(래퍼 없음, status로 분기). */
public record QuestionResponse(String status, JsonNode answer, String message) {
}
