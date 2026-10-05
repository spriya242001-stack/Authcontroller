package com.example.Smartspend_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @SuppressWarnings("unused")
    public void sendVerificationEmail(String to, String token) {
        String subject = "Smartspend - Account Verification";
        String verificationUrl = baseUrl() + "/verify.html?code=" + token;
        String body = "Thank you for registering with Smartspend!\n\n"
                + "Please click the link below to verify your account:\n"
                + verificationUrl + "\n\n"
                + "If you did not initiate this request, please ignore this email.";

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String url = baseUrl() + "/reset-password.html?token=" + token;
        sendEmail(to, "SmartSpend - Reset your password",
                "Use this link to reset your password (valid for 30 minutes):\n\n" + url
                        + "\n\nIf you did not request this, ignore this email.");
    }

    private String baseUrl() {
        return frontendUrl.replaceAll("/+$", "");
    }
}
