package com.projectx.backend.global.api;

public record ApiResponse<T>(boolean success, T data, ApiError error) {

	public static <T> ApiResponse<T> onSuccess(T data) {
		return new ApiResponse<>(true, data, null);
	}

}
