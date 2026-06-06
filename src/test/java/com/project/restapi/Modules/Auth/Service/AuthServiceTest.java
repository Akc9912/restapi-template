package com.project.restapi.Modules.Auth.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.restapi.Modules.Auth.api.dto.LoginRequest;
import com.project.restapi.Modules.Auth.api.dto.PasswordResetConfirmRequest;
import com.project.restapi.Modules.Auth.api.dto.RefreshTokenRequest;
import com.project.restapi.Modules.Auth.api.dto.RegisterRequest;
import com.project.restapi.Modules.Auth.api.dto.VerifyEmailRequest;
import com.project.restapi.Modules.Auth.Entity.AuthTokens;
import com.project.restapi.Modules.Auth.Entity.RefreshToken;
import com.project.restapi.Modules.Auth.Enums.TokenType;
import com.project.restapi.Modules.Users.Enums.Role;
import com.project.restapi.Modules.Users.Entity.User;
import com.project.restapi.Modules.Users.Repository.UserRepository;
import com.project.restapi.Shared.Exception.BadRequestException;
import com.project.restapi.Shared.Exception.UnauthorizedException;
import com.project.restapi.security.JwtProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUser() {
        var request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setPhone("123456789");

        when(userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(a -> {
            var user = a.getArgument(0, User.class);
            return User.class.getDeclaredConstructor().newInstance();
        });
        when(tokenService.generateVerificationToken(any())).thenReturn("verify-token");
        when(jwtProvider.generateAccessToken(any(), any())).thenReturn("access-token");
        when(tokenService.generateRefreshToken(any())).thenReturn("refresh-token");

        var response = authService.register(request);
        assertNotNull(response);
        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        var request = new RegisterRequest();
        request.setEmail("existing@test.com");

        when(userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void shouldLoginSuccessfully() {
        var request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("password123");

        var user = User.builder()
                .id(UUID.randomUUID())
                .hashPassword("encoded")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(userRepository.findByEmailAndDeletedAtIsNullAndEnabledTrue(request.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getHashPassword()))
                .thenReturn(true);
        when(jwtProvider.generateAccessToken(user.getId(), user.getRole()))
                .thenReturn("access-token");
        when(tokenService.generateRefreshToken(user.getId())).thenReturn("refresh-token");

        var response = authService.login(request);
        assertNotNull(response);
    }

    @Test
    void shouldThrowWhenInvalidCredentials() {
        var request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("wrong");

        when(userRepository.findByEmailAndDeletedAtIsNullAndEnabledTrue(request.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void shouldVerifyEmail() {
        var request = new VerifyEmailRequest();
        request.setToken("verify-token");

        var userId = UUID.randomUUID();
        var authToken = AuthTokens.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenType(TokenType.VERIFICATION)
                .build();
        var user = User.builder().id(userId).enabled(false).build();

        when(tokenService.validateAuthToken(request.getToken(), TokenType.VERIFICATION))
                .thenReturn(authToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        authService.verifyEmail(request);
        assertTrue(user.isEnabled());
        verify(userRepository).save(user);
        verify(tokenService).markAuthTokenAsUsed(authToken.getId());
    }

    @Test
    void shouldRefreshToken() {
        var request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh");

        var userId = UUID.randomUUID();
        var refreshToken = RefreshToken.builder()
                .userId(userId)
                .build();
        var user = User.builder().id(userId).role(Role.USER).build();

        when(tokenService.validateRefreshToken(request.getRefreshToken()))
                .thenReturn(refreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(userId, Role.USER)).thenReturn("new-access");
        when(tokenService.generateRefreshToken(userId)).thenReturn("new-refresh");

        var response = authService.refreshToken(request);
        assertNotNull(response);
        verify(tokenService).revokeRefreshToken(request.getRefreshToken());
    }

    @Test
    void shouldConfirmPasswordReset() {
        var request = new PasswordResetConfirmRequest();
        request.setToken("reset-token");
        request.setNewPassword("new-password");

        var userId = UUID.randomUUID();
        var authToken = AuthTokens.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenType(TokenType.PASSWORD_RESET)
                .build();
        var user = User.builder().id(userId).hashPassword("old").build();

        when(tokenService.validateAuthToken(request.getToken(), TokenType.PASSWORD_RESET))
                .thenReturn(authToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new-encoded");

        authService.confirmPasswordReset(request);
        assertEquals("new-encoded", user.getHashPassword());
        verify(userRepository).save(user);
        verify(tokenService).markAuthTokenAsUsed(authToken.getId());
        verify(tokenService).revokeAllUserRefreshTokens(userId);
    }
}
