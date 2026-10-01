package com.example.Smartspend_backend.controller;
import com.example.Smartspend_backend.dto.AuthRequest;
import com.example.Smartspend_backend.dto.AuthResponse;
import com.example.Smartspend_backend.dto.PasswordResetRequest;
import com.example.Smartspend_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        try {
            AuthResponse response = authService.login(request);
            logger.info("Login response: {}", response);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("code") String code) {
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Verification code is required");
        }
        try {
            authService.verifyAccount(code);
            return ResponseEntity.ok("Account verified.");
        } catch (RuntimeException e) {
            logger.error("Verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> processForgotPassword(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        try {
            authService.processForgotPassword(email);
            return ResponseEntity.ok("Password reset link sent.");
        } catch (RuntimeException e) {
            logger.error("Forgot password failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("User not found: " + email);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and new password are required");
        }
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Password reset successful.");
        } catch (RuntimeException e) {
            logger.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Password reset failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        try {
            AuthResponse response = authService.register(request);
            logger.info("Register response: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
}