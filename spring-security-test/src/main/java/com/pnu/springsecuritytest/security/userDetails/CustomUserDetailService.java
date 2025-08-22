package com.pnu.springsecuritytest.security.userDetails;

import com.pnu.springsecuritytest.common.mapper.EntityMapper;
import com.pnu.springsecuritytest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(UUID.fromString(username))
                .map(EntityMapper::toUserInfoDto)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자 입니다." + username));
    }
}
