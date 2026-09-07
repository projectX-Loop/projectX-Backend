package com.projectx.backend.explanation.api;

import jakarta.validation.constraints.NotBlank;

/** 세션 내 이전 질문·답변 한 쌍. 서버가 plan_explanation에 저장은 하지만, ai-service 호출에 실을 history는 지금은
 * 프론트가 그대로 동봉해 보내는 값을 쓴다(9/7 MVP, 프론트 계약 변경 없음). */
public record QuestionHistoryItem(@NotBlank String question, @NotBlank String answer) {
}
