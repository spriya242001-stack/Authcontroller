package com.example.Smartspend_backend.controller;
import com.example.Smartspend_backend.dto.AuthRequest;
import com.example.Smartspend_backend.dto.AuthResponse;
import com.example.Smartspend_backend.dto.PasswordResetRequest;
import com.example.Smartspend_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("code") String code) {
        authService.verifyAccount(code);
        return ResponseEntity.ok("Account verified.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> processForgotPassword(@RequestParam("email") String email) {
        authService.processForgotPassword(email);
        return ResponseEntity.ok("Password reset link sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        authService.resetPassword(request); // Must match AuthService signature
        return ResponseEntity.ok("Password reset successful.");
    }
}