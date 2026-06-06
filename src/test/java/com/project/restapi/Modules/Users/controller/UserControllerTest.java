package com.project.restapi.Modules.Users.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
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
import com.project.restapi.Modules.Users.api.UserApi;
import com.project.restapi.Modules.Users.api.dto.UpdateUserRequest;
import com.project.restapi.Modules.Users.api.dto.UserResponse;
import com.project.restapi.Modules.Users.Enums.Role;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserApi userApi;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    private UserResponse createUserResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .phone("123456789")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    void shouldGetUserById() throws Exception {
        var user = createUserResponse();
        when(userApi.getById(user.getId())).thenReturn(user);

        mockMvc.perform(get("/api/users/v1/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userApi.getAll()).thenReturn(List.of(createUserResponse()));

        mockMvc.perform(get("/api/users/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        var user = createUserResponse();
        var request = new UpdateUserRequest();
        request.setFirstName("Jane");

        when(userApi.update(eq(user.getId()), any())).thenReturn(
                UserResponse.builder()
                        .id(user.getId())
                        .firstName("Jane")
                        .lastName("Doe")
                        .email("john@test.com")
                        .phone("123456789")
                        .role(Role.USER)
                        .enabled(true)
                        .build());

        mockMvc.perform(patch("/api/users/v1/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(userApi).delete(id);

        mockMvc.perform(delete("/api/users/v1/" + id))
                .andExpect(status().isNoContent());

        verify(userApi).delete(id);
    }
}
