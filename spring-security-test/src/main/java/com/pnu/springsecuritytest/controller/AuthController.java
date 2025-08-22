package com.pnu.springsecuritytest.controller;

import com.pnu.springsecuritytest.dto.auth.LoginRequest;
import com.pnu.springsecuritytest.dto.auth.SignupRequest;
import com.pnu.springsecuritytest.dto.auth.TokenResponse;
import com.pnu.springsecuritytest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest
        ) throws AuthenticationException {
        return ResponseEntity.ok(authService.login(
                loginRequest.email(),
                loginRequest.password()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signUp(
            @RequestBody SignupRequest signupRequest
        ) {
        return ResponseEntity.ok(authService.signUp(
                signupRequest.email(),
                signupRequest.password(),
                signupRequest.confirmPassword(),
                signupRequest.age(),
                signupRequest.name()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @AuthenticationPrincipal UserDetails userDetails
            ) throws AuthenticationException {
        return ResponseEntity.ok(authService.refreshToken(userDetails));
    }

    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.logout(userDetails);
        return ResponseEntity.noContent().build();
    }
}
