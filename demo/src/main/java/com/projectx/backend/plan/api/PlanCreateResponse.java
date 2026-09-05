package com.projectx.backend.plan.api;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record PlanCreateResponse(@JsonProperty("public_id") UUID publicId, JsonNode calculation) {

}
