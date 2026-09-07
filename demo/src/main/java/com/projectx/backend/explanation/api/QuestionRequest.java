package com.projectx.backend.explanation.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code POST /api/v1/plans/{publicId}/questions} 요청 본문 — KAN-24.
 *
 * <p>{@code history}는 기존 KAN-4 공개 계약 필드라 계속 받지만(하위 호환), 9/6부터 실제로는 쓰지 않는다 —
 * {@link com.projectx.backend.explanation.application.ExplanationService}가 plan_explanation에 저장된
 * 최근 질문·답변에서 문맥을 직접 재구성한다({@code docs/plan-rag-design.md}). 클라이언트가 보내도 무시된다.
 */
public record QuestionRequest(
		@NotBlank @Size(max = 500) String question,
		@Valid List<QuestionHistoryItem> history) {

	public QuestionRequest {
		if (history == null) {
			history = List.of();
		}
	}

}
