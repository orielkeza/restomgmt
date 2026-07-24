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

    public void sendPaymentSuccessEmail(String toEmail, String username, Long orderId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Payment Confirmed - Order #" + orderId);
        message.setText(
            "Hi " + username + ",\n\n" +
            "Your payment for order #" + orderId + " has been confirmed.\n\n" +
            "Your order is now being prepared. You can track your order status by logging in.\n\n" +
            "Thank you for your order!\n\n" +
            "RestoManagement Team"
        );
        mailSender.send(message);
        log.info("Payment success email sent to {}", toEmail);
    }

    public void sendPaymentFailedEmail(String toEmail, String username, Long orderId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Payment Failed - Order #" + orderId);
        message.setText(
            "Hi " + username + ",\n\n" +
            "Unfortunately your payment for order #" + orderId + " has failed.\n\n" +
            "Please try again by logging in and initiating a new payment.\n\n" +
            "If you continue to have issues, please contact us.\n\n" +
            "RestoManagement Team"
        );
        mailSender.send(message);
        log.info("Payment failed email sent to {}", toEmail);
    }

    public void sendAdminCreatedAccountEmail(String toEmail, String username, 
                                            String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your RestoManagement Account");
        message.setText(
            "Hi " + username + ",\n\n" +
            "An account has been created for you on RestoManagement.\n\n" +
            "Your login details are:\n" +
            "Username: " + username + "\n" +
            "Temporary Password: " + temporaryPassword + "\n\n" +
            "Please log in and change your password immediately.\n\n" +
            "RestoManagement Team"
        );
        mailSender.send(message);
        log.info("Admin created account email sent to {}", toEmail);
    }
}
