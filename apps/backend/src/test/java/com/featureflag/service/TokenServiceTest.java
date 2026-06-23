package com.featureflag.service;

import com.featureflag.config.FfConfig;
import com.featureflag.domain.ApiToken;
import com.featureflag.domain.ApiTokenStatus;
import com.featureflag.domain.Application;
import com.featureflag.dto.ApiTokenCreatedDto;
import com.featureflag.dto.ApiTokenMetadataDto;
import com.featureflag.exception.NotFoundException;
import com.featureflag.exception.UnauthorizedException;
import com.featureflag.repository.ApiTokenRepository;
import com.featureflag.repository.ApplicationRepository;
import com.featureflag.security.AuthenticatedApplication;
import com.featureflag.security.TokenCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    ApiTokenRepository tokenRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    FfConfig config;

    @Mock
    FfConfig.Token tokenConfig;

    @InjectMocks
    TokenService tokenService;

    private static final String PEPPER = "test-pepper";
    private Application app;
    private String plaintext;
    private ApiToken apiToken;

    @BeforeEach
    void setUp() {
        lenient().when(config.token()).thenReturn(tokenConfig);
        lenient().when(tokenConfig.pepper()).thenReturn(PEPPER);

        app = new Application();
        app.id = UUID.randomUUID();
        app.name = "my-app";

        plaintext = TokenCrypto.generatePlaintextToken();
        apiToken = new ApiToken();
        apiToken.id = UUID.randomUUID();
        apiToken.applicationId = app.id;
        apiToken.tokenHash = TokenCrypto.hashToken(plaintext, PEPPER);
        apiToken.tokenPrefix = TokenCrypto.extractPrefix(plaintext);
        apiToken.status = ApiTokenStatus.ACTIVE;
    }

    @Test
    void authenticate_validToken_returnsApplication() {
        when(applicationRepository.findByName("my-app")).thenReturn(Optional.of(app));
        when(tokenRepository.findActiveByHash(apiToken.tokenHash)).thenReturn(Optional.of(apiToken));

        AuthenticatedApplication auth = tokenService.authenticate("my-app", plaintext);

        assertEquals(app.id, auth.getApplicationId());
        assertEquals("my-app", auth.getApplicationName());
    }

    @Test
    void authenticate_missingHeaders_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> tokenService.authenticate(null, "token"));
    }

    @Test
    void authenticate_invalidToken_throwsUnauthorized() {
        when(applicationRepository.findByName("my-app")).thenReturn(Optional.of(app));
        when(tokenRepository.findActiveByHash(any())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> tokenService.authenticate("my-app", "bad"));
    }

    @Test
    void generateToken_persistsHashedToken() {
        when(applicationRepository.findByIdOptional(app.id)).thenReturn(Optional.of(app));
        doAnswer(inv -> {
            ApiToken t = inv.getArgument(0);
            t.id = UUID.randomUUID();
            return null;
        }).when(tokenRepository).persist(any(ApiToken.class));

        ApiTokenCreatedDto created = tokenService.generateToken(app.id, null);

        assertNotNull(created.plaintextToken());
        assertTrue(created.plaintextToken().startsWith("ff_live_"));
        verify(tokenRepository).persist(any(ApiToken.class));
    }

    @Test
    void revokeToken_marksRevoked() {
        when(applicationRepository.findByIdOptional(app.id)).thenReturn(Optional.of(app));
        when(tokenRepository.findByIdAndApplicationId(apiToken.id, app.id)).thenReturn(Optional.of(apiToken));

        ApiTokenMetadataDto result = tokenService.revokeToken(app.id, apiToken.id);

        assertEquals(ApiTokenStatus.REVOKED, result.status());
        assertNotNull(apiToken.revokedAt);
    }

    @Test
    void listTokens_returnsMetadataOnly() {
        when(applicationRepository.findByIdOptional(app.id)).thenReturn(Optional.of(app));
        when(tokenRepository.findByApplicationId(app.id)).thenReturn(List.of(apiToken));

        List<ApiTokenMetadataDto> tokens = tokenService.listTokens(app.id);

        assertEquals(1, tokens.size());
        assertEquals(apiToken.tokenPrefix, tokens.get(0).tokenPrefix());
    }

    @Test
    void revokeToken_notFound_throws() {
        when(applicationRepository.findByIdOptional(app.id)).thenReturn(Optional.of(app));
        when(tokenRepository.findByIdAndApplicationId(any(), eq(app.id))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tokenService.revokeToken(app.id, UUID.randomUUID()));
    }
}
