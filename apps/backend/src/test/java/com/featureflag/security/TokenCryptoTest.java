package com.featureflag.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenCryptoTest {

    @Test
    void generateAndHash_producesConsistentHash() {
        String token = TokenCrypto.generatePlaintextToken();
        assertTrue(token.startsWith("ff_live_"));
        String hash1 = TokenCrypto.hashToken(token, "pepper");
        String hash2 = TokenCrypto.hashToken(token, "pepper");
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, TokenCrypto.hashToken(token, "other"));
    }

    @Test
    void constantTimeEquals_works() {
        assertTrue(TokenCrypto.constantTimeEquals("abc", "abc"));
        assertFalse(TokenCrypto.constantTimeEquals("abc", "abd"));
        assertFalse(TokenCrypto.constantTimeEquals(null, "abc"));
    }
}
