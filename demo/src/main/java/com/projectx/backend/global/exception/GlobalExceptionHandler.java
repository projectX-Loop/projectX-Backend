package com.projectx.backend.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.projectx.backend.global.api.ApiResponse;
import com.projectx.backend.global.api.ValidationError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		return ResponseEntity.status(exception.getErrorCode().getStatus())
				.body(ApiResponse.onFailure(exception.getErrorCode(), exception.getMessage(), exception.getErrors()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
		List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(this::toValidationError)
				.toList();
		return ResponseEntity.badRequest()
				.body(ApiResponse.onFailure(ErrorCode.INVALID_REQUEST, "Invalid request", errors));
	}

	private ValidationError toValidationError(FieldError error) {
		return new ValidationError(error.getField(), error.getDefaultMessage());
	}

}
