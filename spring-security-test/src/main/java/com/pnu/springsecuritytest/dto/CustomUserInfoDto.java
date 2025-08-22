package com.pnu.springsecuritytest.dto;

import java.util.Set;

public record CustomUserInfoDto (
    String id,
    String password,
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean credentialsNonExpired,
    Boolean accountNonLocked,
    Set<String> roles
) {

}
