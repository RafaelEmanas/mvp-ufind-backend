package com.ufind.ufindapp.controller;

import com.ufind.ufindapp.dto.AuthRequest;
import com.ufind.ufindapp.dto.AuthResponse;
import com.ufind.ufindapp.dto.RegisterRequest;
import com.ufind.ufindapp.security.JwtProperties;
import com.ufind.ufindapp.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        addTokenCookie(response, authResponse.token());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        addTokenCookie(response, authResponse.token());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        var cookie = org.springframework.http.ResponseCookie.from(jwtProperties.getCookieName(), token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.getExpiration()))
                .sameSite("Strict")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearTokenCookie(HttpServletResponse response) {
        var cookie = org.springframework.http.ResponseCookie.from(jwtProperties.getCookieName(), "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}