package com.pnu.springsecuritytest.security;

import com.pnu.springsecuritytest.security.exception.CustomAccessDeniedHandler;
import com.pnu.springsecuritytest.security.exception.CustomAuthenticationEntryPoint;
import com.pnu.springsecuritytest.security.jwt.JwtAuthenticationFilter;
import com.pnu.springsecuritytest.security.jwt.TokenProvider;
import com.pnu.springsecuritytest.security.userDetails.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

    private final CustomUserDetailService userDetailService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final TokenProvider tokenProvider;

    public WebSecurityConfig(
            CustomUserDetailService userDetailService,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            TokenProvider tokenProvider
    ) {
        this.userDetailService = userDetailService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(
                tokenProvider,
                userDetailService,
                authenticationEntryPoint
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                // api 서버 이므로 login, logout은 사용하지 않음
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // jwt 토큰을 사용하므로 session 은 STATELESS 로 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // jwt 토큰을 사용하므로 기본적인 csrf 보호는 비활성화
                .csrf(CsrfConfigurer<HttpSecurity>::disable)

                // same-origin 정책을 사용하
                .headers(headers
                        -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // whitelist 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                // jwt 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                // - 이 필터는 JWT 토큰을 검증하고, 인증 정보를 SecurityContext에 설정
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 사용자 정의 AuthenticationEntryPoint 설정
                // - 인증 실패 시 사용자 정의 예외 처리 로직을 사용
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .build();
    }

}
