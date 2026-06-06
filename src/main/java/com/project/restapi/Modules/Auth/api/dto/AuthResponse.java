package com.project.restapi.Modules.Auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.project.restapi.Modules.Users.api.dto.UserResponse;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
