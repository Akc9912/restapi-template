package com.project.restapi.security;

import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.restapi.Modules.Users.Enums.Role;
import com.project.restapi.Shared.Exception.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtProvider {

    @Value("${jwt.secret:}")
    String secret;

    @Value("${jwt.expiration-hours:24}")
    long expirationHours;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (secret.isBlank()) {
            secret = generateDefaultSecret();
        }
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(UUID userId, Role role) {
        var now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + expirationHours * 3600 * 1000))
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new UnauthorizedException("Token JWT inválido o expirado");
        }
    }

    public UUID getUserIdFromToken(String token) {
        var claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    private String generateDefaultSecret() {
        var key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
