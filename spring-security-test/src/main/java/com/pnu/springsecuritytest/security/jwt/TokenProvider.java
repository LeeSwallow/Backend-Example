package com.pnu.springsecuritytest.security.jwt;


import com.pnu.springsecuritytest.common.model.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class TokenProvider {
    private final SecretKey secretKey;
    private final String issuer;
    private final Long accessTokenExpirationMillis;
    private final Long refreshTokenExpirationMillis;

    public TokenProvider (
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access_token.expiration}") Long accessTokenExpiration,
        @Value("${jwt.refresh_token.expiration}") Long refreshTokenExpiration,
        @Value("${jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMillis = accessTokenExpiration * 1000; // seconds to milliseconds
        this.refreshTokenExpirationMillis = refreshTokenExpiration * 1000; // seconds to milliseconds
        this.issuer = issuer;
    }

    public TokenInfo generateAccessToken(String sub) {
        return generateToken(sub, accessTokenExpirationMillis);
    }

    public TokenInfo generateRefreshToken(String sub) {
        return generateToken(sub, refreshTokenExpirationMillis);
    }

    public TokenInfo generateToken(String sub, Long expirationMillis) {
        LocalDateTime expirationLocal = LocalDateTime.now().plusSeconds(expirationMillis / 1000);
        Date now = new Date();
        Date expiration = Date.from(expirationLocal.atZone(ZoneId.systemDefault()).toInstant());
        String token = Jwts.builder()
                .subject(sub)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        return new TokenInfo(token, expirationLocal);
    }

    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public void validateToken(String token) {
        try {
            parseToken(token);
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT 토큰이 만료되었습니다.", e);
        } catch (Exception e) {
            throw new JwtException("잘못된 형식의 JWT 토큰입니다.", e);
        }
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token);
    }

    private Claims parseClaims(String token) {
        try {
            return parseToken(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}