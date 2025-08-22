package com.pnu.springsecuritytest.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    
}
