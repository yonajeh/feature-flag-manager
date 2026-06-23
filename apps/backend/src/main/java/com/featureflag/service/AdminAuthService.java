package com.featureflag.service;

import com.featureflag.config.FfConfig;
import com.featureflag.dto.LoginRequest;
import com.featureflag.dto.LoginResponse;
import com.featureflag.exception.UnauthorizedException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AdminAuthService {

    private static final long EXPIRES_IN_SECONDS = 3600;

    @Inject
    FfConfig config;

    public LoginResponse login(LoginRequest request) {
        if (!config.admin().username().equals(request.username())
                || !config.admin().password().equals(request.password())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = Jwt.issuer(System.getProperty("mp.jwt.verify.issuer", "feature-flag-manager"))
                .upn(request.username())
                .groups(Set.of("super-admin"))
                .audience("feature-flag-admin")
                .expiresIn(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .sign();

        return new LoginResponse(token, EXPIRES_IN_SECONDS);
    }
}
