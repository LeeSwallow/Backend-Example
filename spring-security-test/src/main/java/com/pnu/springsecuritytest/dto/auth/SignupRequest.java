package com.pnu.springsecuritytest.dto.auth;

public record SignupRequest(
        String email,
        String password,
        String confirmPassword,
        Integer age,
        String name
) {
}
