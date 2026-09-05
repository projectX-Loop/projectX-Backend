package com.projectx.backend.plan.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectx.backend.global.api.ApiResponse;
import com.projectx.backend.plan.application.PlanCreateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

	private final PlanCreateService planCreateService;

	@PostMapping
	public ResponseEntity<ApiResponse<PlanCreateResponse>> create(@Valid @RequestBody PlanCreateRequest request) {
		PlanCreateResponse response = planCreateService.create(request.toCommand());
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response));
	}

}
