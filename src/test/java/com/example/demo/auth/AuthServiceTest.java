package com.example.demo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.security.DatabaseUserDetailsService;
import com.example.demo.security.JwtProperties;
import com.example.demo.security.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private DatabaseUserDetailsService userDetailsService;

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i + 1);
        }
        JwtProperties properties = new JwtProperties();
        properties.setSecret(java.util.Base64.getEncoder().encodeToString(key));
        properties.setIssuer("demo1-test");
        jwtService = new JwtService(properties);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, properties,
                authenticationManager, userDetailsService);
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("neto");
        request.setEmail("neto@example.com");
        request.setPassword("password123");
        request.setFullName("Neto Mabjaia");
        request.setProvince("Maputo");
        return request;
    }

    @Test
    void registerPersistsUserAndReturnsTokens() {
        User saved = new User();
        saved.setId(1L);
        saved.setUsername("neto");
        saved.setEmail("neto@example.com");

        when(userRepository.existsByEmail("neto@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("neto")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername("neto").password("$2a$hash").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("neto")).thenReturn(details);

        AuthResponse response = authService.register(registerRequest());

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().username()).isEqualTo("neto");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("neto@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    void loginAuthenticatesAndReturnsTokens() {
        User user = new User();
        user.setId(1L);
        user.setUsername("neto");
        user.setEmail("neto@example.com");
        user.setRole(Role.USER);

        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername("neto").password("hash").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("neto")).thenReturn(details);
        when(userRepository.findByUsername("neto")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setUsername("neto");
        request.setPassword("password123");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isNotBlank();
        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
    }
}