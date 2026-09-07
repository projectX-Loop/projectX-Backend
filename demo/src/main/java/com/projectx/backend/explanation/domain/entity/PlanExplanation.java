package com.projectx.backend.explanation.domain.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan당 AI 상호작용 한 건 (KAN-23·KAN-24 통합, 9/5 결정). 세션 테이블 없이 {@code plan_id}로 바로 그룹핑하고
 * {@code created_at} 순으로 그 plan의 대화 스레드 전체가 된다. 한 행에 요청(question)과 응답(status·payload)을
 * 같이 담아 request/response 2행 분리를 없앴다.
 */
@Entity
@Table(name = "plan_explanation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanExplanation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "plan_id", nullable = false)
	private Long planId;

	@Enumerated(EnumType.STRING)
	@Column(name = "kind", nullable = false, length = 16)
	private ExplanationKind kind;

	@Column(name = "question")
	private String question;

	/** ai-service status 그대로: OK / EXPLANATION_REJECTED / EXPLANATION_UNAVAILABLE / ANSWER_REJECTED / ANSWER_UNAVAILABLE. */
	@Column(name = "status", nullable = false, length = 32)
	private String status;

	/** status=OK면 explanation 또는 answer(Claim) JSON, 아니면 {"message": "..."}. 원문 JSON 문자열 그대로 저장. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", nullable = false, columnDefinition = "jsonb")
	private String payload;

	/** 인용된 지식 청크 참조 배열의 JSON 문자열. 없으면 "[]". */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "retrieved_refs", nullable = false, columnDefinition = "jsonb")
	private String retrievedRefs;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	public static PlanExplanation explanation(Long planId, String status, String payloadJson,
			String retrievedRefsJson) {
		return create(planId, ExplanationKind.EXPLANATION, null, status, payloadJson, retrievedRefsJson);
	}

	public static PlanExplanation question(Long planId, String question, String status, String payloadJson,
			String retrievedRefsJson) {
		if (question == null || question.isBlank()) {
			throw new IllegalArgumentException("question must not be blank for QUESTION kind");
		}
		return create(planId, ExplanationKind.QUESTION, question, status, payloadJson, retrievedRefsJson);
	}

	private static PlanExplanation create(Long planId, ExplanationKind kind, String question, String status,
			String payloadJson, String retrievedRefsJson) {
		PlanExplanation planExplanation = new PlanExplanation();
		planExplanation.planId = planId;
		planExplanation.kind = kind;
		planExplanation.question = question;
		planExplanation.status = status;
		planExplanation.payload = payloadJson;
		planExplanation.retrievedRefs = retrievedRefsJson;
		return planExplanation;
	}

	public boolean isOk() {
		return "OK".equals(status);
	}

}
