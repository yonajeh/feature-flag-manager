package com.featureflag.dto;

import java.util.Map;

public record ConsumerFeatureFlagDto(
        String key, boolean enabled, String description, Map<String, Object> metadata) {}
