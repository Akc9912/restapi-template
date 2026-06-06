package com.project.restapi.Modules.Users.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.restapi.Modules.Users.Entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByEmailAndDeletedAtIsNullAndEnabledTrue(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
