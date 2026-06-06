package com.project.restapi.Modules.Auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.restapi.Modules.Auth.api.AuthApi;
import com.project.restapi.Modules.Auth.api.dto.AuthResponse;
import com.project.restapi.Modules.Auth.api.dto.LoginRequest;
import com.project.restapi.Modules.Auth.api.dto.RegisterRequest;
import com.project.restapi.Modules.Users.Enums.Role;
import com.project.restapi.Modules.Users.api.dto.UserResponse;
import com.project.restapi.Shared.Exception.BadRequestException;
import com.project.restapi.Shared.Exception.GlobalExceptionHandler;
import com.project.restapi.Shared.Exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthApi authApi;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        var request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setPhone("123456789");

        var userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .phone("123456789")
                .role(Role.USER)
                .enabled(false)
                .build();

        when(authApi.register(any())).thenReturn(
                AuthResponse.builder()
                        .accessToken("access")
                        .refreshToken("refresh")
                        .user(userResponse)
                        .build());

        mockMvc.perform(post("/api/auth/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.user.email").value("john@test.com"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        var request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("password123");

        when(authApi.login(any())).thenReturn(
                AuthResponse.builder()
                        .accessToken("access")
                        .refreshToken("refresh")
                        .user(UserResponse.builder().build())
                        .build());

        mockMvc.perform(post("/api/auth/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenInvalidCredentials() throws Exception {
        var request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("wrong");

        when(authApi.login(any())).thenThrow(new UnauthorizedException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenRegisterEmailExists() throws Exception {
        var request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setPhone("123456789");

        when(authApi.register(any())).thenThrow(new BadRequestException("El email ya está registrado"));

        mockMvc.perform(post("/api/auth/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        var request = new RegisterRequest();
        request.setEmail("invalid");

        mockMvc.perform(post("/api/auth/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
