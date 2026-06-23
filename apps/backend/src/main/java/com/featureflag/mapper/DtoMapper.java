package com.featureflag.mapper;

import com.featureflag.domain.ApiToken;
import com.featureflag.domain.Application;
import com.featureflag.domain.FeatureFlag;
import com.featureflag.dto.*;

public final class DtoMapper {

    private DtoMapper() {}

    public static ApplicationDto toDto(Application app) {
        return new ApplicationDto(app.id, app.name, app.displayName, app.createdAt, app.updatedAt);
    }

    public static ApiTokenMetadataDto toMetadataDto(ApiToken token) {
        return new ApiTokenMetadataDto(
                token.id,
                token.applicationId,
                token.tokenPrefix,
                token.status,
                token.lastUsedAt,
                token.expiresAt,
                token.createdAt,
                token.revokedAt);
    }

    public static ApiTokenCreatedDto toCreatedDto(ApiToken token, String plaintext) {
        return new ApiTokenCreatedDto(
                token.id,
                token.applicationId,
                token.tokenPrefix,
                token.status,
                token.lastUsedAt,
                token.expiresAt,
                token.createdAt,
                token.revokedAt,
                plaintext);
    }

    public static FeatureFlagDto toDto(FeatureFlag flag) {
        return new FeatureFlagDto(
                flag.id,
                flag.applicationId,
                flag.key,
                flag.enabled,
                flag.description,
                flag.createdAt,
                flag.updatedAt);
    }

    public static ConsumerFeatureFlagDto toConsumerDto(FeatureFlag flag) {
        return new ConsumerFeatureFlagDto(flag.key, flag.enabled, flag.description);
    }
}
