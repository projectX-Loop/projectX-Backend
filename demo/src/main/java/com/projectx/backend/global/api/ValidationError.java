package com.projectx.backend.global.api;

public record ValidationError(String code, String field, String message) {

	public ValidationError(String field, String message) {
		this(null, field, message);
	}

}
