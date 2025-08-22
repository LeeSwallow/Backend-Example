package com.pnu.springsecuritytest.security.userDetails;

import com.pnu.springsecuritytest.dto.auth.CustomUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final CustomUserInfo userInfo;

    public CustomUserDetails(CustomUserInfo userInfo){
        this.userInfo = userInfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userInfo.roles()
                .stream()
                .map(role -> (GrantedAuthority) () -> role)
                .toList();
    }

    @Override
    public String getPassword() {
        return userInfo.password();
    }

    @Override
    public String getUsername() {
        return userInfo.id();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userInfo.accountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userInfo.accountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userInfo.credentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return userInfo.enabled();
    }
}
