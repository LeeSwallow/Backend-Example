package com.pnu.springsecuritytest.security.jwt;

import com.pnu.springsecuritytest.common.exception.JwtAuthenticationException;
import com.pnu.springsecuritytest.security.exception.CustomAuthenticationEntryPoint;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(
            TokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            CustomAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 토큰 추출 -> 없으면 예외 발생
            String token = Objects.requireNonNull(resolveToken(request));

            // 토큰 유효성 검사 -> 유효하지 않으면 예외 발생
            tokenProvider.validateToken(token);
            String userId = tokenProvider.getUserIdFromToken(token);

            // 사용자 정보 로드 -> 사용자가 존재하지 않으면 예외 발생
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);


            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null, // jwt 토큰을 사용하므로 비밀번호는 null로 설정
                    userDetails.getAuthorities()
            );

            // 인증 객체에 추가 정보 설정(로그인 IP, 세션 등)
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 객체 설정
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 다음 필터로 요청 전달
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException(
                    "JWT 토큰이 만료되었습니다. refresh 토큰을 사용하거나 재로그인하세요.", e.getCause()
            ));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException(
                    "JWT 토큰이 유효하지 않습니다. Authorization 헤더를 확인하세요.", e.getCause()
            ));
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
