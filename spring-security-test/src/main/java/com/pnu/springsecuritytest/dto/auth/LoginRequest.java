package com.pnu.springsecuritytest.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
