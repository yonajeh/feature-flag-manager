package com.featureflag.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationRequest(@NotBlank String displayName) {}
