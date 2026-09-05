package com.projectx.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST),
	GOAL_AMOUNT_RANGE(HttpStatus.BAD_REQUEST),
	GOAL_HORIZON_RANGE(HttpStatus.BAD_REQUEST),
	FUNDS_INITIAL_RANGE(HttpStatus.BAD_REQUEST),
	FUNDS_MONTHLY_RANGE(HttpStatus.BAD_REQUEST),
	NO_FUNDS(HttpStatus.BAD_REQUEST),
	ALLOC_SUM_INITIAL(HttpStatus.BAD_REQUEST),
	ALLOC_SUM_MONTHLY(HttpStatus.BAD_REQUEST),
	PORTFOLIO_REQUIRED(HttpStatus.BAD_REQUEST),
	PORTFOLIO_WEIGHT_RANGE(HttpStatus.BAD_REQUEST),
	WEIGHTS_SUM(HttpStatus.BAD_REQUEST),
	PORTFOLIO_ASSET_DUP(HttpStatus.BAD_REQUEST),
	ASSET_NOT_IN_CATALOG(HttpStatus.BAD_REQUEST),
	FOCUS_INVALID(HttpStatus.BAD_REQUEST),
	DATA_SNAPSHOT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
	CALCULATOR_UNAVAILABLE(HttpStatus.BAD_GATEWAY),
	CALCULATOR_DATA_MISMATCH(HttpStatus.BAD_GATEWAY),
	CALCULATOR_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY);

	private final HttpStatus status;

	ErrorCode(HttpStatus status) {
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
