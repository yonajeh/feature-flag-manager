package com.featureflag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateFeatureFlagRequest(
        @NotBlank @Pattern(regexp = "^[a-z][a-z0-9_-]*$") String key,
        @NotNull Boolean enabled,
        String description) {}
