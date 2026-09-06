package com.projectx.backend.plan.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectx.backend.plan.application.SampleQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
public class SamplesController {

	private final SampleQueryService sampleQueryService;

	@GetMapping
	public ResponseEntity<SamplesResponse> getSamples() {
		return ResponseEntity.ok(sampleQueryService.getSamples());
	}

}
