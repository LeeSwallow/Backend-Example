package com.pnu.springsecuritytest.service;

import com.pnu.springsecuritytest.common.model.TokenInfo;
import com.pnu.springsecuritytest.dto.auth.TokenResponse;
import com.pnu.springsecuritytest.entity.RefreshToken;
import com.pnu.springsecuritytest.entity.User;
import com.pnu.springsecuritytest.repository.RefreshTokenRepository;
import com.pnu.springsecuritytest.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    private TokenResponse createTokenResponse(User user) {
        String sub = user.getId().toString();
        TokenInfo accessToken = tokenProvider.generateAccessToken(sub);
        TokenInfo refreshToken = tokenProvider.generateRefreshToken(sub);

        refreshTokenRepository.save(new RefreshToken(refreshToken, user));

        return new TokenResponse(accessToken.value(), refreshToken.value());
    }

    public TokenResponse login(String email, String password) throws AuthenticationException {
        User user;
        try {
            user = userService.getByEmail(email);
        } catch (Exception e) {
            throw new AuthenticationException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
        return createTokenResponse(user);
    }

    public TokenResponse signUp(String email, String password, String confirmPassword, Integer age, String name) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        User user = userService.create(new User(email, password, age, name));
        return createTokenResponse(user);
    }

    public TokenResponse refreshToken(UserDetails userDetails) throws AuthenticationException {
        User user = userService.getById(UUID.fromString(userDetails.getUsername()));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new AuthenticationException("리프레시 토큰이 존재하지 않습니다. 로그인 후 다시 시도해주세요."));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("리프레시 토큰이 만료되었습니다. 로그인 후 다시 시도해주세요.");
        }

        return createTokenResponse(user);
    }

    public void logout(UserDetails userDetails) {
        User user = userService.getById(UUID.fromString(userDetails.getUsername()));
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
    }
}
