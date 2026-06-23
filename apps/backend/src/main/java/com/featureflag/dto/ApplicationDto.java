package com.featureflag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;

public record ApplicationDto(
        UUID id,
        String name,
        String displayName,
        Instant createdAt,
        Instant updatedAt) {}
