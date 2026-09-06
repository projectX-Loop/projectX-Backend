package com.projectx.backend.plan.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectx.backend.plan.application.PlanCreateService;
import com.projectx.backend.plan.application.PlanQueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

	private final PlanCreateService planCreateService;
	private final PlanQueryService planQueryService;

	@PostMapping
	public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanCreateRequest request) {
		PlanResponse response = planCreateService.create(request.toCommand());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{publicId}")
	public ResponseEntity<PlanResponse> get(@PathVariable UUID publicId) {
		return ResponseEntity.ok(planQueryService.get(publicId));
	}

}
