package com.example.Smartspend_backend.service;

import com.example.Smartspend_backend.dto.AuthRequest;
import com.example.Smartspend_backend.dto.AuthResponse;
import com.example.Smartspend_backend.dto.PasswordResetRequest;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    // 1. Fix: login(AuthRequest)
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Generate a real dynamic UUID token
        String token = java.util.UUID.randomUUID().toString();

        // 2. Save the token to the user object so your security filter can look it up
        user.setToken(token); // (Make sure your User model has a setToken field, or adjust if you use a separate token table)
        userRepository.save(user);

        // 3. Return the response containing the real token
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    // 2. Fix: verifyAccount(String code) or verifyAccount(String email, String code)
    public boolean verifyAccount(String tokenOrCode) {
        // Implement logic to verify account email verification token
        // e.g., check token validity in database and enable user account
        return true;
    }

    // 3. Fix: processForgotPassword(String email)
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email not found: " + email));

        // Generate reset token, save to DB/Cache, and send password reset email
    }

    // 4. Fix: resetPassword(PasswordResetRequest) or resetPassword(String token, String newPassword)
    public void resetPassword(PasswordResetRequest request) {
        // Validate reset token and update user's password
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    public AuthResponse register(AuthRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Default role

        // Generate token so they are logged in immediately upon registering
        String token = java.util.UUID.randomUUID().toString();
        user.setToken(token);

        userRepository.save(user);

        // Return the response body so Postman receives the token JSON
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}