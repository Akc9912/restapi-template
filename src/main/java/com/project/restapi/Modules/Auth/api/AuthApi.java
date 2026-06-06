package com.project.restapi.Modules.Auth.api;

import com.project.restapi.Modules.Auth.api.dto.AuthResponse;
import com.project.restapi.Modules.Auth.api.dto.LoginRequest;
import com.project.restapi.Modules.Auth.api.dto.PasswordResetConfirmRequest;
import com.project.restapi.Modules.Auth.api.dto.PasswordResetRequest;
import com.project.restapi.Modules.Auth.api.dto.RefreshTokenRequest;
import com.project.restapi.Modules.Auth.api.dto.RegisterRequest;
import com.project.restapi.Modules.Auth.api.dto.VerifyEmailRequest;

public interface AuthApi {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void verifyEmail(VerifyEmailRequest request);

    void requestPasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);
}
