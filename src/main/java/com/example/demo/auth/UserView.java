package com.example.demo.auth;

import com.example.demo.user.Role;

public record UserView(Long id,
                       String username,
                       String email,
                       String fullName,
                       String province,
                       Role role) {
}