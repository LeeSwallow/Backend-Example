package com.pnu.springsecuritytest.dto.user;

public record UserEditRequest(
    String name,
    Integer age,
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean accountNonLocked,
    Boolean credentialsNonExpired
) {

}
