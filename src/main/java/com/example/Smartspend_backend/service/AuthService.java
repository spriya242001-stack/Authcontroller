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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.example.Smartspend_backend.model.VerificationToken;
import com.example.Smartspend_backend.repository.VerificationTokenRepository;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private VerificationTokenRepository verificationTokens;

    @Autowired
    private EmailService emailService;

    @Value("${app.verification-email-enabled:true}")
    private boolean verificationEmailEnabled = true;

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

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(context);

        // 3. Return the response containing the real token
        return new AuthResponse(token, user.getEmail(), user.getRole(), user.getId());
    }

    // 2. Fix: verifyAccount(String code) or verifyAccount(String email, String code)
    @Transactional
    public boolean verifyAccount(String tokenOrCode) {
        VerificationToken token = validToken(tokenOrCode, "VERIFY");
        token.getUser().setEmailVerified(true);
        userRepository.save(token.getUser());
        verificationTokens.delete(token);
        return true;
    }

    // 3. Fix: processForgotPassword(String email)
    @Transactional
    public void processForgotPassword(String email) {
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            String token = issueToken(user, "RESET", 30);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    // 4. Fix: resetPassword(PasswordResetRequest) or resetPassword(String token, String newPassword)
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        VerificationToken token = validToken(request.getToken(), "RESET");
        User user = token.getUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setToken(null);
        userRepository.save(user);
        verificationTokens.delete(token);
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Default role

        // Generate token so they are logged in immediately upon registering
        String token = java.util.UUID.randomUUID().toString();
        user.setToken(token);

        userRepository.save(user);

        if (verificationEmailEnabled) {
            emailService.sendVerificationEmail(user.getEmail(), issueToken(user, "VERIFY", 1440));
        }

        // Return the response body so Postman receives the token JSON
        return new AuthResponse(token, user.getEmail(), user.getRole(), user.getId());
    }

    private String issueToken(User user, String purpose, int minutes) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String raw = HexFormat.of().formatHex(bytes);
        VerificationToken record = verificationTokens.findByUser(user).orElseGet(VerificationToken::new);
        record.setUser(user);
        record.setToken(hash(raw));
        record.setPurpose(purpose);
        record.setExpiryDate(LocalDateTime.now().plusMinutes(minutes));
        verificationTokens.save(record);
        return raw;
    }

    private VerificationToken validToken(String raw, String purpose) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("A valid email link is required");
        }
        VerificationToken record = verificationTokens.findByToken(hash(raw))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired email link"));
        if (!purpose.equals(record.getPurpose()) || record.getExpiryDate() == null
                || !record.getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired email link");
        }
        return record;
    }

    private String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
