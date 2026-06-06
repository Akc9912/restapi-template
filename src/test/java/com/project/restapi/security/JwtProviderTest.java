package com.project.restapi.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.restapi.Modules.Users.Enums.Role;
import com.project.restapi.Shared.Exception.UnauthorizedException;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        jwtProvider.secret = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGZvciBqd3QgdG9rZW4gZ2VuZXJhdGlvbg==";
        jwtProvider.expirationHours = 24;
        jwtProvider.init();
    }

    @Test
    void shouldGenerateAndValidateToken() {
        var userId = UUID.randomUUID();
        var token = jwtProvider.generateAccessToken(userId, Role.USER);
        assertNotNull(token);

        var claims = jwtProvider.validateToken(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(Role.USER.name(), claims.get("role"));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        var userId = UUID.randomUUID();
        var token = jwtProvider.generateAccessToken(userId, Role.ADMIN);
        var extracted = jwtProvider.getUserIdFromToken(token);
        assertEquals(userId, extracted);
    }

    @Test
    void shouldThrowWhenTokenIsInvalid() {
        assertThrows(UnauthorizedException.class,
                () -> jwtProvider.validateToken("invalid-token"));
    }
}
