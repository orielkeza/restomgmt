package com.restomgmt.site.user.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String username, String token) {
        String link = "http://localhost:8080/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your email - RestoManagement");
        message.setText(
            "Hi " + username + ",\n\n" +
            "Please verify your email address by clicking the link below:\n\n" +
            link + "\n\n" +
            "This link expires in 24 hours.\n\n" +
            "If you did not create an account, you can safely ignore this email.\n\n" +
            "RestoManagement Team"
        );

        mailSender.send(message);
        log.info("Verification email sent to {}", toEmail);
    }

    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        String link = "http://localhost:8080/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your password - RestoManagement");
        message.setText(
            "Hi " + username + ",\n\n" +
            "You requested a password reset. Click the link below to reset your password:\n\n" +
            link + "\n\n" +
            "This link expires in 30 minutes.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "RestoManagement Team"
        );

        mailSender.send(message);
        log.info("Password reset email sent to {}", toEmail);
    }
}
