package com.project.restapi.Modules.Users.api;

import java.util.List;
import java.util.UUID;

import com.project.restapi.Modules.Users.api.dto.ChangePasswordRequest;
import com.project.restapi.Modules.Users.api.dto.UpdateUserRequest;
import com.project.restapi.Modules.Users.api.dto.UserResponse;

public interface UserApi {

    UserResponse getById(UUID id);

    UserResponse getByEmail(String email);

    List<UserResponse> getAll();

    UserResponse update(UUID id, UpdateUserRequest request);

    void delete(UUID id);

    void changePassword(UUID id, ChangePasswordRequest request);
}
