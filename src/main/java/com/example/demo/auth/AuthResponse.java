package com.example.demo.auth;

public record AuthResponse(String accessToken,
                           String refreshToken,
                           String tokenType,
                           long expiresIn,
                           UserView user) {

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserView user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}