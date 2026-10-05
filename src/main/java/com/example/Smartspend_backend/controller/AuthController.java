package com.example.Smartspend_backend.controller;
import com.example.Smartspend_backend.dto.AuthRequest;
import com.example.Smartspend_backend.dto.AuthResponse;
import com.example.Smartspend_backend.dto.PasswordResetRequest;
import com.example.Smartspend_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        try {
            AuthResponse response = authService.login(request);
            if (servletRequest.getSession(false) != null) {
                servletRequest.changeSessionId();
            }
            new HttpSessionSecurityContextRepository().saveContext(
                    SecurityContextHolder.getContext(), servletRequest, servletResponse);
            logger.info("Login successful for {}", response.getEmail());
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
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
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
            return ResponseEntity.ok("If an account exists, a password reset link has been sent.");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Forgot password failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Unable to send email right now. Please try again later.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and new password are required");
        }
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Password reset successful.");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Password reset failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        try {
            AuthResponse response = authService.register(request);
            logger.info("Registration successful for {}", response.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
}
