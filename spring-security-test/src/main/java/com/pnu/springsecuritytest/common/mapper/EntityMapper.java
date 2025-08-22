package com.pnu.springsecuritytest.common.mapper;

import com.pnu.springsecuritytest.dto.auth.CustomUserInfo;
import com.pnu.springsecuritytest.dto.user.UserResponse;
import com.pnu.springsecuritytest.entity.User;

public class EntityMapper {

    private EntityMapper() {
        // private constructor to prevent instantiation
    }

    public static CustomUserInfo toUserInfoDto(User user) {
        return new CustomUserInfo(
            user.getId().toString(),
            user.getPassword(),
            user.getEnabled(),
            user.getAccountNonExpired(),
            user.getCredentialsNonExpired(),
            user.getAccountNonLocked(),
            user.getRoleTypes().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet())
        );
    }

    public static UserResponse toDto(User user) {
        return new UserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getAge(),
            user.getName(),
            user.getRoleTypes().stream().toList(),
            user.getProviderTypes().stream().toList(),
            user.getEnabled(),
            user.getAccountNonExpired(),
            user.getAccountNonLocked(),
            user.getCredentialsNonExpired()
        );
    }
}
