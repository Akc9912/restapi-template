package com.project.restapi.Modules.Users.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.restapi.Modules.Users.Entity.User;
import com.project.restapi.Modules.Users.Enums.Role;
import com.project.restapi.Modules.Users.Repository.UserRepository;
import com.project.restapi.Modules.Users.api.dto.ChangePasswordRequest;
import com.project.restapi.Modules.Users.api.dto.UpdateUserRequest;
import com.project.restapi.Shared.Exception.BadRequestException;
import com.project.restapi.Shared.Exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createUser(UUID id) {
        return User.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .phone("123456789")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    void shouldGetUserById() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var response = userService.getById(id);
        assertEquals(id, response.getId());
        assertEquals("john@test.com", response.getEmail());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(id));
    }

    @Test
    void shouldThrowWhenUserDeleted() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        user.setDeletedAt(java.time.LocalDateTime.now());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(id));
    }

    @Test
    void shouldGetAllActiveUsers() {
        var user1 = createUser(UUID.randomUUID());
        var user2 = createUser(UUID.randomUUID());
        user2.setDeletedAt(java.time.LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        var users = userService.getAll();
        assertEquals(1, users.size());
    }

    @Test
    void shouldUpdateUser() {
        var id = UUID.randomUUID();
        var user = createUser(id);

        var request = new UpdateUserRequest();
        request.setFirstName("Jane");
        request.setPhone("987654321");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(a -> a.getArgument(0));

        var response = userService.update(id, request);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("987654321", response.getPhone());
    }

    @Test
    void shouldSoftDeleteUser() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.delete(id);
        assertNotNull(user.getDeletedAt());
        verify(userRepository).save(user);
    }

    @Test
    void shouldChangePassword() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        user.setHashPassword("old-encoded");

        var request = new ChangePasswordRequest();
        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getHashPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new-encoded");

        userService.changePassword(id, request);
        assertEquals("new-encoded", user.getHashPassword());
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenCurrentPasswordWrong() {
        var id = UUID.randomUUID();
        var user = createUser(id);
        user.setHashPassword("old-encoded");

        var request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong-password");
        request.setNewPassword("new-password");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getHashPassword()))
                .thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.changePassword(id, request));
    }
}
