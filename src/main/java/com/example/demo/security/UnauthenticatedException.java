package com.example.demo.security;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Authentication is required");
    }
}