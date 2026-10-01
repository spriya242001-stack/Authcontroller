package com.example.Smartspend_backend.security;

import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unused")
public class JwtService {

    public String generateToken(String username) {
        // Token generation logic here (using io.jsonwebtoken / jjwt)
        return "generated_jwt_token";
    }
}