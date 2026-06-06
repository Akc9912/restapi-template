package com.project.restapi.Modules.Auth.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.restapi.Modules.Auth.Entity.AuthTokens;
import com.project.restapi.Modules.Auth.Entity.RefreshToken;
import com.project.restapi.Modules.Auth.Enums.TokenType;
import com.project.restapi.Modules.Auth.Repository.AuthTokensRepository;
import com.project.restapi.Modules.Auth.Repository.RefreshTokenRepository;
import com.project.restapi.Shared.Exception.BadRequestException;
import com.project.restapi.Shared.Exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private AuthTokensRepository authTokensRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void shouldGenerateVerificationToken() {
        var userId = UUID.randomUUID();
        when(authTokensRepository.save(any())).thenAnswer(a -> a.getArgument(0));

        var token = tokenService.generateVerificationToken(userId);
        assertNotNull(token);
        verify(authTokensRepository).save(any());
    }

    @Test
    void shouldGeneratePasswordResetToken() {
        var userId = UUID.randomUUID();
        when(authTokensRepository.save(any())).thenAnswer(a -> a.getArgument(0));

        var token = tokenService.generatePasswordResetToken(userId);
        assertNotNull(token);
        verify(authTokensRepository).save(any());
    }

    @Test
    void shouldGenerateRefreshToken() {
        var userId = UUID.randomUUID();
        when(refreshTokenRepository.save(any())).thenAnswer(a -> a.getArgument(0));

        var token = tokenService.generateRefreshToken(userId);
        assertNotNull(token);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void shouldValidateAuthToken() {
        var rawToken = "raw-token";
        var hashedToken = tokenService.hashToken(rawToken);
        var authToken = AuthTokens.builder()
                .id(UUID.randomUUID())
                .tokenHash(hashedToken)
                .tokenType(TokenType.VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(authTokensRepository.findByTokenHashAndUsedFalse(hashedToken))
                .thenReturn(Optional.of(authToken));

        var result = tokenService.validateAuthToken(rawToken, TokenType.VERIFICATION);
        assertNotNull(result);
        assertEquals(authToken.getId(), result.getId());
    }

    @Test
    void shouldThrowWhenAuthTokenExpired() {
        var rawToken = "expired-token";
        var hashedToken = tokenService.hashToken(rawToken);
        var authToken = AuthTokens.builder()
                .tokenHash(hashedToken)
                .tokenType(TokenType.VERIFICATION)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(authTokensRepository.findByTokenHashAndUsedFalse(hashedToken))
                .thenReturn(Optional.of(authToken));

        assertThrows(BadRequestException.class,
                () -> tokenService.validateAuthToken(rawToken, TokenType.VERIFICATION));
    }

    @Test
    void shouldThrowWhenAuthTokenAlreadyUsed() {
        var rawToken = "used-token";
        when(authTokensRepository.findByTokenHashAndUsedFalse(any()))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> tokenService.validateAuthToken(rawToken, TokenType.VERIFICATION));
    }

    @Test
    void shouldMarkAuthTokenAsUsed() {
        var tokenId = UUID.randomUUID();
        var authToken = AuthTokens.builder().id(tokenId).build();
        when(authTokensRepository.findById(tokenId)).thenReturn(Optional.of(authToken));

        tokenService.markAuthTokenAsUsed(tokenId);
        assertTrue(authToken.isUsed());
        verify(authTokensRepository).save(authToken);
    }

    @Test
    void shouldThrowWhenMarkingNonExistentToken() {
        var tokenId = UUID.randomUUID();
        when(authTokensRepository.findById(tokenId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tokenService.markAuthTokenAsUsed(tokenId));
    }

    @Test
    void shouldRevokeRefreshToken() {
        var rawToken = "refresh-token";
        var hashedToken = tokenService.hashToken(rawToken);
        var refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .tokenHash(hashedToken)
                .isActive(true)
                .build();

        when(refreshTokenRepository.findByTokenHashAndIsActiveTrue(hashedToken))
                .thenReturn(Optional.of(refreshToken));

        tokenService.revokeRefreshToken(rawToken);
        assertFalse(refreshToken.isActive());
        assertNotNull(refreshToken.getRevokedAt());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void shouldRevokeAllUserRefreshTokens() {
        var userId = UUID.randomUUID();
        var refreshToken = RefreshToken.builder().isActive(true).build();
        when(refreshTokenRepository.findByUserIdAndIsActiveTrue(userId))
                .thenReturn(Optional.of(refreshToken));

        tokenService.revokeAllUserRefreshTokens(userId);
        assertFalse(refreshToken.isActive());
        assertNotNull(refreshToken.getRevokedAt());
        verify(refreshTokenRepository).save(refreshToken);
    }
}
