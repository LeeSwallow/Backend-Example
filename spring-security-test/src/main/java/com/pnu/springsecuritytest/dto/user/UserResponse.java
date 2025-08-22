package com.pnu.springsecuritytest.dto.user;

import com.pnu.springsecuritytest.entity.type.ProviderType;
import com.pnu.springsecuritytest.entity.type.RoleType;

import java.util.List;

public record UserResponse(
        String id,
        String email,
        Integer age,
        String name,
        List<RoleType> roles,
        List<ProviderType> providers,
        Boolean enabled,
        Boolean accountNonExpired,
        Boolean accountNonLocked,
        Boolean credentialsNonExpired
) {


}
