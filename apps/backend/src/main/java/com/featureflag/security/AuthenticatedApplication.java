package com.featureflag.security;

import java.util.UUID;

public class AuthenticatedApplication {
    private final UUID applicationId;
    private final String applicationName;
    private final UUID tokenId;

    public AuthenticatedApplication(UUID applicationId, String applicationName, UUID tokenId) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.tokenId = tokenId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public UUID getTokenId() {
        return tokenId;
    }
}
