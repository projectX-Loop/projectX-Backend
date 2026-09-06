package com.projectx.backend.asset.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UniverseResponse(Snapshot snapshot, List<UniverseAsset> assets) {

	public record Snapshot(@JsonProperty("data_version") String dataVersion, @JsonProperty("data_hash") String dataHash,
			Window window) {
	}

	public record Window(String start, String end, int months) {
	}

	public record UniverseAsset(String code, @JsonProperty("display_name") String displayName, String instrument,
			@JsonProperty("tax_class") String taxClass) {
	}

}
