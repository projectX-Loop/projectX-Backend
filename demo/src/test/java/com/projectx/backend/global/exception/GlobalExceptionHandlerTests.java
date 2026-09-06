package com.projectx.backend.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.projectx.backend.global.api.ApiError;
import com.projectx.backend.global.api.ValidationError;
import com.projectx.backend.plan.api.PlanController;
import com.projectx.backend.plan.application.PlanCreateService;
import com.projectx.backend.plan.application.PlanQueryService;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void returnsFrontendErrorContractForBusinessException() {
		ResponseEntity<ApiError> response = handler.handleBusinessException(
				new BusinessException(ErrorCode.GOAL_AMOUNT_RANGE,
						List.of(new ValidationError("goal.amount", "goal.amount must be between 1000000 and 10000000000"))));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(new ApiError("PLAN-400-1", "Goal amount is out of range", false,
				"goal.amount", List.of(new ValidationError("PLAN-400-1", "goal.amount",
						"goal.amount must be between 1000000 and 10000000000")), null, null));
	}

	@Test
	void marksUnavailableDependenciesAsRetryable() {
		ResponseEntity<ApiError> response = handler.handleBusinessException(
				new BusinessException(ErrorCode.CALCULATOR_UNAVAILABLE));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
		assertThat(response.getBody()).isEqualTo(new ApiError("PLAN-502-1", "Calculator is unavailable", true, null,
				List.of(), null, null));
	}

	@Test
	void includesPublicIdForRetryableCalculationFailure() {
		UUID publicId = UUID.randomUUID();
		ResponseEntity<ApiError> response = handler.handleBusinessException(
				new BusinessException(ErrorCode.CALCULATION_FAILED, publicId));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
		assertThat(response.getBody().publicId()).isEqualTo(publicId.toString());
	}

	@Test
	void returnsGenericErrorForUnexpectedRuntimeException() {
		ResponseEntity<ApiError> response = handler.handleRuntimeException(new RuntimeException("unexpected"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isEqualTo(new ApiError("COMMON-500", "Internal server error", true, null,
				List.of(), null, null));
	}

	@Test
	void returnsFrontendErrorJsonWithoutSuccessWrapper() throws Exception {
		MockMvc mockMvc = MockMvcBuilders
				.standaloneSetup(new PlanController(mock(PlanCreateService.class), mock(PlanQueryService.class)))
				.setControllerAdvice(handler)
				.build();

		mockMvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("COMMON-400"))
				.andExpect(jsonPath("$.message").value("Invalid request"))
				.andExpect(jsonPath("$.retryable").value(false))
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.success").doesNotExist())
				.andExpect(jsonPath("$.error").doesNotExist());
	}

}
