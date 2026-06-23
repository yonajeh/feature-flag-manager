package com.featureflag.dto;

public record LoginResponse(String token, long expiresIn) {}
