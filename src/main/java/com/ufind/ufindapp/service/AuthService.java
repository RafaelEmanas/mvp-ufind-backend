package com.ufind.ufindapp.service;

import com.ufind.ufindapp.dto.AuthRequest;
import com.ufind.ufindapp.dto.AuthResponse;
import com.ufind.ufindapp.dto.RegisterRequest;
import com.ufind.ufindapp.entity.User;
import com.ufind.ufindapp.exception.UserAlreadyExistsException;
import com.ufind.ufindapp.repository.UserRepository;
import com.ufind.ufindapp.security.JwtService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        UserDetailsService userDetailsService) {
            
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            return new AuthResponse(token);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Bad Credentials.");
        }
    }

    public AuthResponse register(RegisterRequest request) {
        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("This user already exists.");
        }

        UserDetails newUserDetails = userDetailsService.loadUserByUsername(newUser.getEmail());
        String token = jwtService.generateToken(newUserDetails);

        return new AuthResponse(token);
    }
}
