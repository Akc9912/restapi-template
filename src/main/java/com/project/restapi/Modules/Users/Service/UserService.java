package com.project.restapi.Modules.Users.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.restapi.Modules.Users.api.UserApi;
import com.project.restapi.Modules.Users.api.dto.ChangePasswordRequest;
import com.project.restapi.Modules.Users.api.dto.UpdateUserRequest;
import com.project.restapi.Modules.Users.api.dto.UserResponse;
import com.project.restapi.Modules.Users.Entity.User;
import com.project.restapi.Modules.Users.Repository.UserRepository;
import com.project.restapi.Shared.Exception.BadRequestException;
import com.project.restapi.Shared.Exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getById(UUID id) {
        var user = findActiveUser(id);
        return toUserResponse(user);
    }

    @Override
    public UserResponse getByEmail(String email) {
        var user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        var user = findActiveUser(id);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var user = findActiveUser(id);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        var user = findActiveUser(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getHashPassword())) {
            throw new BadRequestException("La contraseña actual no es correcta");
        }

        user.setHashPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findActiveUser(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        return user;
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
