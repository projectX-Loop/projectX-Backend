package com.projectx.backend.asset.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectx.backend.asset.application.UniverseQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/universe")
@RequiredArgsConstructor
public class UniverseController {

	private final UniverseQueryService universeQueryService;

	@GetMapping
	public ResponseEntity<UniverseResponse> getUniverse() {
		return ResponseEntity.ok(universeQueryService.getUniverse());
	}

}
