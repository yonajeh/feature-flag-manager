package com.featureflag.dto;

import com.featureflag.domain.ApiTokenStatus;
import java.time.Instant;
import java.util.UUID;

public record ApiTokenCreatedDto(
        UUID id,
        UUID applicationId,
        String tokenPrefix,
        ApiTokenStatus status,
        Instant lastUsedAt,
        Instant expiresAt,
        Instant createdAt,
        Instant revokedAt,
        String plaintextToken) {}
