package com.featureflag.dto;

import java.util.List;

public record ExportedApplication(
        ApplicationDto application,
        List<FeatureFlagDto> featureFlags,
        List<ApiTokenMetadataDto> tokens) {}
