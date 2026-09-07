package com.projectx.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "Invalid request", false),
	TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON-400-1", "Request parameter has an invalid type", false),
	MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400-2", "Request body is malformed", false),
	MULTIPART_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "COMMON-400-3", "Multipart request is invalid", false),
	ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "COMMON-400-4", "Request cannot be processed in the current state", false),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "Internal server error", true),
	GOAL_AMOUNT_RANGE(HttpStatus.BAD_REQUEST, "PLAN-400-1", "Goal amount is out of range", false),
	GOAL_HORIZON_RANGE(HttpStatus.BAD_REQUEST, "PLAN-400-2", "Goal horizon is out of range", false),
	FUNDS_INITIAL_RANGE(HttpStatus.BAD_REQUEST, "PLAN-400-3", "Initial funds must not be negative", false),
	FUNDS_MONTHLY_RANGE(HttpStatus.BAD_REQUEST, "PLAN-400-4", "Monthly funds must not be negative", false),
	NO_FUNDS(HttpStatus.BAD_REQUEST, "PLAN-400-5", "At least one fund amount is required", false),
	ALLOC_SUM_INITIAL(HttpStatus.BAD_REQUEST, "PLAN-400-6", "Initial allocation is invalid", false),
	ALLOC_SUM_MONTHLY(HttpStatus.BAD_REQUEST, "PLAN-400-7", "Monthly allocation is invalid", false),
	PORTFOLIO_REQUIRED(HttpStatus.BAD_REQUEST, "PLAN-400-8", "Portfolio assets are required", false),
	PORTFOLIO_WEIGHT_RANGE(HttpStatus.BAD_REQUEST, "PLAN-400-9", "Portfolio asset weight is out of range", false),
	WEIGHTS_SUM(HttpStatus.BAD_REQUEST, "PLAN-400-10", "Portfolio asset weights must sum to 100", false),
	PORTFOLIO_ASSET_DUP(HttpStatus.BAD_REQUEST, "PLAN-400-11", "Portfolio asset codes must be unique", false),
	ASSET_NOT_IN_CATALOG(HttpStatus.BAD_REQUEST, "PLAN-400-12", "Portfolio asset is not in the current catalog", false),
	FOCUS_INVALID(HttpStatus.BAD_REQUEST, "PLAN-400-13", "Rebalancing focus is invalid", false),
	DATA_SNAPSHOT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "PLAN-503-1", "Current data snapshot is unavailable", true),
	PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN_NOT_FOUND", "Plan not found", false),
	CALCULATION_FAILED(HttpStatus.BAD_GATEWAY, "CALCULATION_FAILED", "Calculation failed", true),
	CALCULATOR_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "PLAN-502-1", "Calculator is unavailable", true),
	CALCULATOR_DATA_MISMATCH(HttpStatus.BAD_GATEWAY, "PLAN-502-2", "Calculator data does not match the plan snapshot", true),
	CALCULATOR_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "PLAN-502-3", "Calculator response is invalid", true),

	// 아래 4개는 explanation 패키지(성종현, KAN-23·24) 전용 — plan 쪽과 개념이 안 겹쳐서 그대로 둔다.
	// PLAN_NOT_FOUND·CALCULATION_FAILED는 위 도윤 정의(plan 패키지, PR #8)를 그대로 재사용한다(9/6, 도윤 기준으로 통일 —
	// 원래 이 자리에 있던 EXPLANATION-404-1/502-1 한글 메시지 버전은 삭제. 아래 「알려진 문제」 참고).
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "EXPLANATION-400-1", "요청 값이 올바르지 않습니다.", false),
	ENGINE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXPLANATION-503-1", "계산 엔진을 사용할 수 없습니다.", true),
	EXPLANATION_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "EXPLANATION-502-2", "AI 설명을 잠시 사용할 수 없습니다.", true),
	ANSWER_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "EXPLANATION-502-3", "지금은 답변할 수 없습니다.", true);

	private final HttpStatus status;
	private final String code;
	private final String message;
	private final boolean retryable;

	ErrorCode(HttpStatus status, String code, String message, boolean retryable) {
		this.status = status;
		this.code = code;
		this.message = message;
		this.retryable = retryable;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRetryable() {
		return retryable;
	}

}
