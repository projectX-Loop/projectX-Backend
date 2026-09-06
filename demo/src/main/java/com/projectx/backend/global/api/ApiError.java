package com.projectx.backend.global.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiError(String code, String message, boolean retryable, String field, List<ValidationError> errors,
		@JsonProperty("public_id") String publicId, @JsonProperty("max_months") Integer maxMonths) {

}
