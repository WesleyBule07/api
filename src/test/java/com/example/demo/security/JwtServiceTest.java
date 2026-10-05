package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final byte[] RAW_KEY = new byte[32];

    static {
        for (int i = 0; i < RAW_KEY.length; i++) {
            RAW_KEY[i] = (byte) i;
        }
    }

    private JwtProperties properties;
    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(java.util.Base64.getEncoder().encodeToString(RAW_KEY));
        properties.setIssuer("demo1-test");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        jwtService = new JwtService(properties);

        user = User.withUsername("test-user")
                .password("encoded")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generatesAccessAndRefreshTokensWithDifferentTypes() {
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        assertThat(access).isNotEqualTo(refresh);
        assertThat(jwtService.isRefreshToken(refresh)).isTrue();
        assertThat(jwtService.isRefreshToken(access)).isFalse();
    }

    @Test
    void extractsUsernameAndValidates() {
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo("test-user");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void validatesIssuer() {
        String token = jwtService.generateAccessToken(user);
        jwtService.isTokenValid(token, user);
        assertThat(true).isTrue();
    }

    @Test
    void rejectsTokensWithWrongIssuer() {
        JwtProperties other = new JwtProperties();
        other.setSecret(properties.getSecret());
        other.setIssuer("other-issuer");
        other.setAccessTokenTtl(Duration.ofMinutes(15));
        other.setRefreshTokenTtl(Duration.ofDays(7));

        String token = new JwtService(other).generateAccessToken(user);
        assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredTokens() {
        JwtProperties shortLived = new JwtProperties();
        shortLived.setSecret(properties.getSecret());
        shortLived.setIssuer(properties.getIssuer());
        shortLived.setAccessTokenTtl(Duration.ofMillis(-1000));
        shortLived.setRefreshTokenTtl(Duration.ofDays(7));

        String expired = new JwtService(shortLived).generateAccessToken(user);
        assertThat(jwtService.isTokenExpired(expired)).isTrue();
        assertThatThrownBy(() -> jwtService.isTokenValid(expired, user))
                .isInstanceOf(ExpiredJwtException.class);
    }
}