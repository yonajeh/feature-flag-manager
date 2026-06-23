package com.featureflag.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenCrypto {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String TOKEN_PREFIX = "ff_live_";

    private TokenCrypto() {}

    public static String generatePlaintextToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return TOKEN_PREFIX + encoded;
    }

    public static String extractPrefix(String plaintext) {
        return plaintext.substring(0, Math.min(8, plaintext.length()));
    }

    public static String hashToken(String plaintext, String pepper) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((pepper + plaintext).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
