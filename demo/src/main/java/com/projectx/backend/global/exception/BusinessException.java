package com.projectx.backend.global.exception;

import java.util.List;

import com.projectx.backend.global.api.ValidationError;

public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;
	private final List<ValidationError> errors;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, List.of());
	}

	public BusinessException(ErrorCode errorCode, List<ValidationError> errors) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.errors = errors;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

}
