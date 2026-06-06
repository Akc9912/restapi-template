package com.project.restapi.Modules.Auth.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.restapi.Modules.Auth.api.AuthApi;
import com.project.restapi.Modules.Auth.api.dto.AuthResponse;
import com.project.restapi.Modules.Auth.api.dto.LoginRequest;
import com.project.restapi.Modules.Auth.api.dto.PasswordResetConfirmRequest;
import com.project.restapi.Modules.Auth.api.dto.PasswordResetRequest;
import com.project.restapi.Modules.Auth.api.dto.RefreshTokenRequest;
import com.project.restapi.Modules.Auth.api.dto.RegisterRequest;
import com.project.restapi.Modules.Auth.api.dto.VerifyEmailRequest;
import com.project.restapi.Modules.Auth.Enums.TokenType;
import com.project.restapi.Modules.Users.Enums.Role;
import com.project.restapi.Modules.Users.Entity.User;
import com.project.restapi.Modules.Users.Repository.UserRepository;
import com.project.restapi.Modules.Users.api.dto.UserResponse;
import com.project.restapi.Shared.Exception.BadRequestException;
import com.project.restapi.Shared.Exception.ResourceNotFoundException;
import com.project.restapi.security.JwtProvider;
import com.project.restapi.Shared.Exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthApi {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .hashPassword(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER)
                .enabled(false)
                .build();

        user = userRepository.save(user);
        tokenService.generateVerificationToken(user.getId());

        var accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        var refreshToken = tokenService.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmailAndDeletedAtIsNullAndEnabledTrue(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas o cuenta no verificada"));

        if (!passwordEncoder.matches(request.getPassword(), user.getHashPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        var accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        var refreshToken = tokenService.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        var refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());

        tokenService.revokeRefreshToken(request.getRefreshToken());

        var user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        var accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        var newRefreshToken = tokenService.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        var authToken = tokenService.validateAuthToken(request.getToken(), TokenType.VERIFICATION);

        var user = userRepository.findById(authToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setEnabled(true);
        userRepository.save(user);
        tokenService.markAuthTokenAsUsed(authToken.getId());
    }

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {
        var user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElse(null);

        if (user != null) {
            tokenService.generatePasswordResetToken(user.getId());
        }
    }

    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        var authToken = tokenService.validateAuthToken(request.getToken(), TokenType.PASSWORD_RESET);

        var user = userRepository.findById(authToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setHashPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenService.markAuthTokenAsUsed(authToken.getId());
        tokenService.revokeAllUserRefreshTokens(user.getId());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
