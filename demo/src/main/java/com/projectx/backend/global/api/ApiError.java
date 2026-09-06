package com.projectx.backend.global.api;

import java.util.List;

public record ApiError(String code, String message, boolean retryable, List<ValidationError> errors) {

}
