package com.featureflag.dto;

public record ConsumerFeatureFlagDto(String key, boolean enabled, String description) {}
