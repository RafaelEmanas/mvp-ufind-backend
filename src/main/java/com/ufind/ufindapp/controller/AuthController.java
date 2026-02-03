package com.ufind.ufindapp.controller;

import com.ufind.ufindapp.dto.LoginRequest;
import com.ufind.ufindapp.dto.LoginDTO;
import com.ufind.ufindapp.dto.RegisterUserRequest;
import com.ufind.ufindapp.security.JwtProperties;
import com.ufind.ufindapp.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> register(
        @Valid
        @RequestBody
        RegisterUserRequest request
    ) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        LoginDTO authResponse = authService.login(request);
        addTokenCookie(response, authResponse.token());
        return ResponseEntity.status(200).build();
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