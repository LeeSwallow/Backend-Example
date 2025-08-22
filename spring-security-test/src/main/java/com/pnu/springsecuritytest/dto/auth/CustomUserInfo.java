package com.pnu.springsecuritytest.dto.auth;

import java.util.Set;

public record CustomUserInfo(
    String id,
    String password,
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean credentialsNonExpired,
    Boolean accountNonLocked,
    Set<String> roles
) {

}
