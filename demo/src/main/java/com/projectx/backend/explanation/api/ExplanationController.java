package com.projectx.backend.explanation.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectx.backend.explanation.application.ExplanationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/plans/{publicId}")
@RequiredArgsConstructor
public class ExplanationController {

	private final ExplanationService explanationService;

	@PostMapping("/explanation")
	public ResponseEntity<ExplanationResponse> explain(@PathVariable UUID publicId) {
		return ResponseEntity.ok(explanationService.explain(publicId));
	}

	@PostMapping("/questions")
	public ResponseEntity<QuestionResponse> ask(@PathVariable UUID publicId,
			@Valid @RequestBody QuestionRequest request) {
		return ResponseEntity.ok(explanationService.ask(publicId, request));
	}

}
