package com.ufind.ufindapp.service;

import com.ufind.ufindapp.dto.LoginRequest;
import com.ufind.ufindapp.dto.LoginDTO;
import com.ufind.ufindapp.dto.RegisterUserRequest;
import com.ufind.ufindapp.entity.User;
import com.ufind.ufindapp.entity.UserRole;
import com.ufind.ufindapp.exception.InvalidRoleException;
import com.ufind.ufindapp.exception.UserAlreadyExistsException;
import com.ufind.ufindapp.repository.UserRepository;
import com.ufind.ufindapp.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
        ) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public LoginDTO login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            return new LoginDTO(token);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Bad Credentials.");
        }
    }

    public void register(RegisterUserRequest request) {

        UserRole role;
        try {
            role = UserRole.valueOf(request.role());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRoleException("Invalid role " + request.role());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("This user already exists.");
        }

        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .build();
        
        userRepository.save(newUser);
                
    }
}
