package com.featureflag.dto;

import java.time.Instant;
import java.util.List;

public record ApplicationDataExportDto(
        Instant exportedAt,
        int version,
        ApplicationDto application,
        List<FeatureFlagDto> featureFlags,
        List<ApiTokenMetadataDto> tokens) {}
