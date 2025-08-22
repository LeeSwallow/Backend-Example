package com.pnu.springsecuritytest.dto.user;

public record UserCreateRequest(
        String email,
        String password,
        Integer age,
        String name
) {
}
