package com.featureflag.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FeatureFlagDto(
        UUID id,
        UUID applicationId,
        String key,
        boolean enabled,
        String description,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt) {}
