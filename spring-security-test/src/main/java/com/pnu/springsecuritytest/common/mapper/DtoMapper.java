package com.pnu.springsecuritytest.common.mapper;

import com.pnu.springsecuritytest.dto.user.UserCreateRequest;
import com.pnu.springsecuritytest.dto.user.UserEditRequest;
import com.pnu.springsecuritytest.entity.User;

import java.util.function.Consumer;

public class DtoMapper {

    private DtoMapper() {
        // Prevent instantiation
    }

    public static Consumer<User> toConsumer(UserEditRequest request) {
        return user -> {
            if (request.age() != null) {
                user.setName(request.name());
            }
            if (request.name() != null) {
                user.setAge(request.age());
            }
            if (request.enabled() != null) {
                user.setEnabled(request.enabled());
            }
            if (request.accountNonExpired() != null) {
                user.setAccountNonExpired(request.accountNonExpired());
            }
            if (request.accountNonLocked() != null) {
                user.setAccountNonLocked(request.accountNonLocked());
            }
            if (request.credentialsNonExpired() != null) {
                user.setCredentialsNonExpired(request.credentialsNonExpired());
            }
        };
    }

    public static User toEntity(UserCreateRequest request) {
        return new User(
                request.email(),
                request.password(),
                request.age(),
                request.name()
        );
    }
}
