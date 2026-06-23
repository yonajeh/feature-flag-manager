package com.featureflag.service;

import com.featureflag.config.FfConfig;
import com.featureflag.dto.LoginRequest;
import com.featureflag.dto.LoginResponse;
import com.featureflag.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    FfConfig config;

    @Mock
    FfConfig.Admin adminConfig;

    @InjectMocks
    AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        lenient().when(config.admin()).thenReturn(adminConfig);
        lenient().when(adminConfig.username()).thenReturn("admin");
        lenient().when(adminConfig.password()).thenReturn("admin");
    }

    @Test
    void login_validCredentials_returnsToken() {
        LoginResponse response = adminAuthService.login(new LoginRequest("admin", "admin"));

        assertNotNull(response.token());
        assertTrue(response.expiresIn() > 0);
    }

    @Test
    void login_invalidCredentials_throws() {
        assertThrows(UnauthorizedException.class, () ->
                adminAuthService.login(new LoginRequest("admin", "wrong")));
    }
}
