package com.project.restapi.Modules.Auth.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.restapi.Modules.Auth.Entity.AuthTokens;
import com.project.restapi.Modules.Auth.Enums.TokenType;

public interface AuthTokensRepository extends JpaRepository<AuthTokens, UUID> {

    Optional<AuthTokens> findByTokenHashAndUsedFalse(String tokenHash);

    Optional<AuthTokens> findByUserIdAndTokenTypeAndUsedFalse(UUID userId, TokenType tokenType);
}
