package com.projectx.backend.global.exception;

import java.util.List;

import com.projectx.backend.global.api.ValidationError;

public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;
	private final List<ValidationError> errors;

	public BusinessException(ErrorCode errorCode, String message) {
		this(errorCode, message, List.of());
	}

	public BusinessException(ErrorCode errorCode, String message, List<ValidationError> errors) {
		super(message);
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
