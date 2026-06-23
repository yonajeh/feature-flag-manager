package com.featureflag.dto;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagDto(
        UUID id,
        UUID applicationId,
        String key,
        boolean enabled,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
