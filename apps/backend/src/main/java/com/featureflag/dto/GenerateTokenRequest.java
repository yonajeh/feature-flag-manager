package com.featureflag.dto;

import java.time.Instant;

public record GenerateTokenRequest(Instant expiresAt) {}
