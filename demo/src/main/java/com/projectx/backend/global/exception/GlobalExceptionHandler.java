package com.projectx.backend.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.projectx.backend.global.api.ApiError;
import com.projectx.backend.global.api.ValidationError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiError> handleBusinessException(BusinessException exception) {
		return response(exception.getErrorCode(), exception.getErrors(), exception.getPublicId());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception) {
		List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(this::toValidationError)
				.toList();
		return response(ErrorCode.INVALID_REQUEST, errors);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException exception) {
		return response(ErrorCode.INVALID_REQUEST);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
		return response(ErrorCode.TYPE_MISMATCH);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleMessageNotReadableException(HttpMessageNotReadableException exception) {
		return response(ErrorCode.MALFORMED_REQUEST);
	}

	@ExceptionHandler({ MissingServletRequestPartException.class, MultipartException.class })
	public ResponseEntity<ApiError> handleMultipartException(Exception exception) {
		return response(ErrorCode.MULTIPART_REQUEST_INVALID);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalStateException(IllegalStateException exception) {
		return response(ErrorCode.ILLEGAL_STATE);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiError> handleRuntimeException(RuntimeException exception) {
		return response(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ApiError> response(ErrorCode errorCode) {
		return response(errorCode, List.of());
	}

	private ResponseEntity<ApiError> response(ErrorCode errorCode, List<ValidationError> errors) {
		return response(errorCode, errors, null);
	}

	private ResponseEntity<ApiError> response(ErrorCode errorCode, List<ValidationError> errors, java.util.UUID publicId) {
		List<ValidationError> details = errors.stream()
				.map(error -> error.code() == null
						? new ValidationError(errorCode.getCode(), error.field(), error.message())
						: error)
				.toList();
		String field = details.size() == 1 ? details.get(0).field() : null;
		ApiError body = new ApiError(errorCode.getCode(), errorCode.getMessage(), errorCode.isRetryable(), field, details,
				publicId == null ? null : publicId.toString(), null);
		return ResponseEntity.status(errorCode.getStatus()).body(body);
	}

	private ValidationError toValidationError(FieldError error) {
		return new ValidationError(ErrorCode.INVALID_REQUEST.getCode(), error.getField(), error.getDefaultMessage());
	}

}
