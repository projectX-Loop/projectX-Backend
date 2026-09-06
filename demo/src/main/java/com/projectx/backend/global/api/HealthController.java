package com.projectx.backend.global.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public ResponseEntity<ApiResponse<HealthResponse>> health() {
		return ResponseEntity.ok(ApiResponse.onSuccess(new HealthResponse("UP")));
	}

	public record HealthResponse(String status) {

	}

}
