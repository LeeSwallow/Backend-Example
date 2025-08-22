package com.pnu.springsecuritytest.common.mapper;

import com.pnu.springsecuritytest.dto.CustomUserInfoDto;
import com.pnu.springsecuritytest.entity.User;

public class EntityMapper {

    private EntityMapper() {
        // private constructor to prevent instantiation
    }

    public static CustomUserInfoDto toUserInfoDto(User user) {
        return new CustomUserInfoDto(
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

}
