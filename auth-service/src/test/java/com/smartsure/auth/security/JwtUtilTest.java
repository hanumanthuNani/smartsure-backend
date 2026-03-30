package com.smartsure.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private String validSecret = "SmartSureSecretKeyForJWTTokenGenerationAndValidation2024";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", validSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_notNull() {
        String token = jwtUtil.generateToken("john@example.com", List.of("ROLE_USER"), 1L);
        assertNotNull(token);
    }

    @Test
    void generateToken_containsEmail() {
        String token = jwtUtil.generateToken("john@example.com", List.of("ROLE_USER"), 1L);
        String email = jwtUtil.extractEmail(token);
        assertEquals("john@example.com", email);
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("john@example.com", List.of("ROLE_USER"), 1L);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("admin@example.com", List.of("ROLE_ADMIN"), 1L);
        String email = jwtUtil.extractEmail(token);
        assertEquals("admin@example.com", email);
    }
}
