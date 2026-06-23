package com.featureflag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateApplicationRequest(
        @NotBlank @Pattern(regexp = "^[a-z][a-z0-9-]*$") String name,
        @NotBlank String displayName) {}
