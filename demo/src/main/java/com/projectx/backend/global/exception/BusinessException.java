package com.projectx.backend.global.exception;

import java.util.List;
import java.util.UUID;

import com.projectx.backend.global.api.ValidationError;

public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;
	private final List<ValidationError> errors;
	private final UUID publicId;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, List.of(), null);
	}

	public BusinessException(ErrorCode errorCode, List<ValidationError> errors) {
		this(errorCode, errors, null);
	}

	public BusinessException(ErrorCode errorCode, UUID publicId) {
		this(errorCode, List.of(), publicId);
	}

	private BusinessException(ErrorCode errorCode, List<ValidationError> errors, UUID publicId) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.errors = errors;
		this.publicId = publicId;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

	public UUID getPublicId() {
		return publicId;
	}

}
