package com.example.demo.name;

public class NameNotFoundException extends RuntimeException {

    public NameNotFoundException(Long id) {
        super("Name not found: " + id);
    }
}