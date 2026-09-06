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
	CALCULATOR_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "PLAN-502-1", "Calculator is unavailable", true),
	CALCULATOR_DATA_MISMATCH(HttpStatus.BAD_GATEWAY, "PLAN-502-2", "Calculator data does not match the plan snapshot", true),
	CALCULATOR_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "PLAN-502-3", "Calculator response is invalid", true);

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
