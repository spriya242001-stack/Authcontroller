package com.example.Smartspend_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private Long userId;

    public AuthResponse(String token, String email, String role) {
        this(token, email, role, null);
    }
}
