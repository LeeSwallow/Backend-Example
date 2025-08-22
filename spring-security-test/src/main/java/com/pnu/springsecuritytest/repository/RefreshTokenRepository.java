package com.pnu.springsecuritytest.repository;

import com.pnu.springsecuritytest.entity.RefreshToken;
import com.pnu.springsecuritytest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByUser(User user);
}
