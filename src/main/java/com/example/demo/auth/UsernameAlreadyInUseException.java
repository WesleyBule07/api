package com.example.demo.auth;

public class UsernameAlreadyInUseException extends RuntimeException {

    public UsernameAlreadyInUseException(String username) {
        super("Username is already in use: " + username);
    }
}