package com.featureflag.service;

import com.featureflag.config.FfConfig;
import com.featureflag.domain.ApiToken;
import com.featureflag.domain.ApiTokenStatus;
import com.featureflag.domain.Application;
import com.featureflag.dto.ApiTokenCreatedDto;
import com.featureflag.dto.ApiTokenMetadataDto;
import com.featureflag.exception.NotFoundException;
import com.featureflag.exception.UnauthorizedException;
import com.featureflag.mapper.DtoMapper;
import com.featureflag.repository.ApiTokenRepository;
import com.featureflag.repository.ApplicationRepository;
import com.featureflag.security.AuthenticatedApplication;
import com.featureflag.security.TokenCrypto;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TokenService {

    @Inject
    ApiTokenRepository tokenRepository;

    @Inject
    ApplicationRepository applicationRepository;

    @Inject
    FfConfig config;

    public AuthenticatedApplication authenticate(String appName, String plaintextToken) {
        if (appName == null || appName.isBlank() || plaintextToken == null || plaintextToken.isBlank()) {
            throw new UnauthorizedException("Missing authentication headers");
        }

        Application app = applicationRepository.findByName(appName)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String hash = TokenCrypto.hashToken(plaintextToken, config.token().pepper());
        ApiToken token = tokenRepository.findActiveByHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!token.applicationId.equals(app.id)) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (token.expiresAt != null && token.expiresAt.isBefore(Instant.now())) {
            throw new UnauthorizedException("Token expired");
        }

        updateLastUsedAsync(token.id);
        return new AuthenticatedApplication(app.id, app.name, token.id);
    }

    void updateLastUsedAsync(UUID tokenId) {
        Thread.startVirtualThread(() -> {
            try {
                updateLastUsed(tokenId);
            } catch (Exception e) {
                Log.warn("Failed to update lastUsedAt for token " + tokenId, e);
            }
        });
    }

    @Transactional
    public void updateLastUsed(UUID tokenId) {
        ApiToken token = tokenRepository.findByIdOptional(tokenId)
                .orElse(null);
        if (token != null) {
            token.lastUsedAt = Instant.now();
            tokenRepository.persist(token);
        }
    }

    @Transactional
    public ApiTokenCreatedDto generateToken(UUID applicationId, Instant expiresAt) {
        requireApplication(applicationId);
        return createToken(applicationId, expiresAt);
    }

    @Transactional
    public ApiTokenCreatedDto rotateToken(UUID applicationId, UUID tokenId) {
        requireApplication(applicationId);
        tokenRepository.findByIdAndApplicationId(tokenId, applicationId)
                .orElseThrow(() -> new NotFoundException("Token not found"));
        return createToken(applicationId, null);
    }

    @Transactional
    public ApiTokenMetadataDto revokeToken(UUID applicationId, UUID tokenId) {
        requireApplication(applicationId);
        ApiToken token = tokenRepository.findByIdAndApplicationId(tokenId, applicationId)
                .orElseThrow(() -> new NotFoundException("Token not found"));
        if (token.status == ApiTokenStatus.REVOKED) {
            return DtoMapper.toMetadataDto(token);
        }
        token.status = ApiTokenStatus.REVOKED;
        token.revokedAt = Instant.now();
        tokenRepository.persist(token);
        return DtoMapper.toMetadataDto(token);
    }

    public List<ApiTokenMetadataDto> listTokens(UUID applicationId) {
        requireApplication(applicationId);
        return tokenRepository.findByApplicationId(applicationId).stream()
                .map(DtoMapper::toMetadataDto)
                .toList();
    }

    private ApiTokenCreatedDto createToken(UUID applicationId, Instant expiresAt) {
        String plaintext = TokenCrypto.generatePlaintextToken();
        ApiToken token = new ApiToken();
        token.applicationId = applicationId;
        token.tokenHash = TokenCrypto.hashToken(plaintext, config.token().pepper());
        token.tokenPrefix = TokenCrypto.extractPrefix(plaintext);
        token.status = ApiTokenStatus.ACTIVE;
        token.expiresAt = expiresAt;
        tokenRepository.persist(token);
        return DtoMapper.toCreatedDto(token, plaintext);
    }

    private Application requireApplication(UUID applicationId) {
        return applicationRepository.findByIdOptional(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
    }
}
