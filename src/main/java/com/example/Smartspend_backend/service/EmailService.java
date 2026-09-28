package com.example.Smartspend_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String token) {
        String subject = "Smartspend - Account Verification";
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String body = "Thank you for registering with Smartspend!\n\n"
                + "Please click the link below to verify your account:\n"
                + verificationUrl + "\n\n"
                + "If you did not initiate this request, please ignore this email.";

        sendEmail(to, subject, body);
    }
}