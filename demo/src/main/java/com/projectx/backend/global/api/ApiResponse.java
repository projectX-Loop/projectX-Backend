package com.projectx.backend.global.api;

import java.util.List;

import com.projectx.backend.global.exception.ErrorCode;

public record ApiResponse<T>(boolean success, T data, ApiError error) {

	public static <T> ApiResponse<T> onSuccess(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> onFailure(ErrorCode errorCode, String message,
			List<ValidationError> errors) {
		return new ApiResponse<>(false, null, new ApiError(errorCode.name(), message, errors));
	}

}
