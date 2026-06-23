package com.featureflag.dto;

import java.util.Map;

public record UpdateFeatureFlagRequest(Boolean enabled, String description, Map<String, Object> metadata) {}
