package com.project.restapi.Modules.Users.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.restapi.Modules.Users.api.UserApi;
import com.project.restapi.Modules.Users.api.dto.ChangePasswordRequest;
import com.project.restapi.Modules.Users.api.dto.UpdateUserRequest;
import com.project.restapi.Modules.Users.api.dto.UserResponse;
import com.project.restapi.Shared.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApi userApi;

    @GetMapping("/v1/me")
    public ResponseEntity<UserResponse> getMe() {
        var userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userApi.getById(userId));
    }

    @GetMapping("/v1/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userApi.getById(id));
    }

    @GetMapping("/v1")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userApi.getAll());
    }

    @PatchMapping("/v1/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userApi.update(id, request));
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userApi.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v1/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userApi.changePassword(id, request);
        return ResponseEntity.ok().build();
    }
}
