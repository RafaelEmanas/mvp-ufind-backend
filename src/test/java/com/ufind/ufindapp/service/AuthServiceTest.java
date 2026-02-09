package com.ufind.ufindapp.service;

import com.ufind.ufindapp.dto.LoginDTO;
import com.ufind.ufindapp.dto.LoginRequest;
import com.ufind.ufindapp.dto.RegisterUserRequest;
import com.ufind.ufindapp.entity.User;
import com.ufind.ufindapp.entity.UserRole;
import com.ufind.ufindapp.exception.InvalidRoleException;
import com.ufind.ufindapp.exception.UserAlreadyExistsException;
import com.ufind.ufindapp.repository.UserRepository;
import com.ufind.ufindapp.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor;

    private LoginRequest validLoginRequest;
    private RegisterUserRequest requestSecretary;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest(
                "maria.santos@icomp.ufam.edu.br",
                "password@2026"
        );

        requestSecretary = new RegisterUserRequest(
            "maria.santos",
            "maria.santos@icomp.ufam.edu.br",
            "password@2026",
            "ROLE_SECRETARY"
        );
    }

    // === Happy Path Tests - User Registration ===

    @Test
    @DisplayName("Should register user with valid data and encoded password")
    void shouldRegisterUserWithValidData() {
        when(userRepository.existsByEmail(requestSecretary.email())).thenReturn(false);
        when(passwordEncoder.encode("password@2026")).thenReturn("encodedPassword");

        authService.register(requestSecretary);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertThat(savedUser.getUsername()).isEqualTo("maria.santos");
        assertThat(savedUser.getEmail()).isEqualTo("maria.santos@icomp.ufam.edu.br");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_SECRETARY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_SECRETARY", "ROLE_ADMIN"})
    @DisplayName("Should accept valid roles")
    void shouldAcceptValidRoles(String validRole) {
        RegisterUserRequest request = new RegisterUserRequest(
            "user", "user@email.com", "password", validRole);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    // === Happy Path Tests - User Login ===

    @Test
    @DisplayName("Should successfully login with valid credentials and return JWT token")
    void shouldLoginSuccessfullyWithValidCredentials() {
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        LoginDTO result = authService.login(validLoginRequest);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("Should authenticate with email and password from request")
    void shouldAuthenticateWithEmailAndPasswordFromRequest() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token"); 

        authService.login(validLoginRequest);

        verify(authenticationManager).authenticate(authTokenCaptor.capture());
        UsernamePasswordAuthenticationToken capturedToken = authTokenCaptor.getValue();
        
        assertThat(capturedToken.getPrincipal()).isEqualTo("maria.santos@icomp.ufam.edu.br");
        assertThat(capturedToken.getCredentials()).isEqualTo("password@2026");
    }

    @Test
    @DisplayName("Should generate JWT token using authenticated UserDetails principal")
    void shouldGenerateTokenUsingAuthenticatedPrincipal() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("generated.jwt.token");

        authService.login(validLoginRequest);

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    @DisplayName("Should return LoginDTO with token generated by JwtService")
    void shouldReturnLoginDTOWithGeneratedToken() {
        String jwtToken = "jwt.token.here";
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(jwtToken);

        LoginDTO result = authService.login(validLoginRequest);

        assertThat(result.token()).isEqualTo(jwtToken);
    }

    @Test
    @DisplayName("Should work for admin user with ROLE_ADMIN authority")
    void shouldWorkForAdminUser() {
        LoginRequest adminRequest = new LoginRequest(
                "admin@icomp.ufam.edu.br",
                "admin@2026"
        );

        UserDetails adminUserDetails =
            org.springframework.security.core.userdetails.User.withUsername("admin@icomp.ufam.edu.br")
            .password("$2a$10$adminEncodedPassword")
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
            .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);
        when(jwtService.generateToken(adminUserDetails)).thenReturn("admin.jwt.token");

        LoginDTO result = authService.login(adminRequest);

        assertThat(result.token()).isEqualTo("admin.jwt.token");
        verify(jwtService).generateToken(adminUserDetails);
    }

    // === Error Scenarios - Invalid Role ===

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_USER", "SECRETARY", "ADMIN", "ROLE_INVALID", "role_secretary"})
    @DisplayName("Should throw InvalidRoleException for invalid roles")
    void shouldThrowInvalidRoleException(String invalidRole) {
        RegisterUserRequest request = new RegisterUserRequest(
            "user", "user@email.com", "password", invalidRole);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(InvalidRoleException.class)
            .hasMessageContaining("Invalid role " + invalidRole);
        
        verify(userRepository, never()).save(any(User.class));
    }

    // === Error Scenarios - Duplicate Email ===

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email exists")
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(requestSecretary.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(requestSecretary))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("This user already exists.");
        
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // === Error Scenarios - Authentication Failures ===

    @Test
    @DisplayName("Should throw BadCredentialsException when authentication fails with wrong password")
    void shouldThrowBadCredentialsExceptionWhenPasswordIsWrong() {
        LoginRequest wrongPasswordRequest = new LoginRequest(
                "maria.santos@icomp.ufam.edu.br",
                "wrongPassword"
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad Credentials.");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when user does not exist")
    void shouldThrowBadCredentialsExceptionWhenUserDoesNotExist() {
        LoginRequest nonExistentUserRequest = new LoginRequest(
                "nonexistent@email.com",
                "password123"
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));

        assertThatThrownBy(() -> authService.login(nonExistentUserRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad Credentials.");
    }

    @Test
    @DisplayName("Should rethrow BadCredentialsException with custom message")
    void shouldRethrowBadCredentialsExceptionWithCustomMessage() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Original authentication error message"));

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad Credentials.")
                .hasMessageNotContaining("Original authentication error message");
    }

    @Test
    @DisplayName("Should NOT generate token when authentication fails")
    void shouldNotGenerateTokenWhenAuthenticationFails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        try {
            authService.login(validLoginRequest);
        } catch (BadCredentialsException e) {
            // expected
        }

        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    // === Execution Order Tests ===

    @Test
    @DisplayName("Should validate role before checking email existence")
    void shouldValidateRoleBeforeCheckingEmail() {
        RegisterUserRequest invalidRequest = new RegisterUserRequest(
            "user", "user@email.com", "password", "INVALID_ROLE");

        assertThatThrownBy(() -> authService.register(invalidRequest))
            .isInstanceOf(InvalidRoleException.class);
        
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Should encode password before saving")
    void shouldEncodePasswordBeforeSaving() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        authService.register(requestSecretary);

        InOrder inOrder = inOrder(passwordEncoder, userRepository);
        inOrder.verify(passwordEncoder).encode("password@2026");
        inOrder.verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should follow correct execution flow: authenticate -> extract principal -> generate token")
    void shouldFollowCorrectExecutionFlow() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        authService.login(validLoginRequest);

        var inOrder = inOrder(authenticationManager, authentication, jwtService);
        inOrder.verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        inOrder.verify(authentication).getPrincipal();
        inOrder.verify(jwtService).generateToken(userDetails);
    }

}
