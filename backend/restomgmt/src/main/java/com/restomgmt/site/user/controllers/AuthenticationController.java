package com.restomgmt.site.user.controllers;

import com.restomgmt.site.user.dto.ForgotPasswordRequest;
import com.restomgmt.site.user.dto.RegisterRequest;
import com.restomgmt.site.user.dto.ResetPasswordRequest;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.security.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) {
        try {
            String token = authenticationService.authenticate(
                user.getUsername(), user.getPassword());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Email not verified. Please check your inbox.");
        } catch (Exception e) {
            log.error("Login error: {}", e.getClass().getName() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Please verify your email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authenticationService.verifyEmail(token);
            return ResponseEntity.ok("Email verified successfully. You can now login.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.requestPasswordReset(request.getEmail());
        // Always return same message for security
        return ResponseEntity.ok(
            "If that email is registered you will receive a reset link shortly.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.resetPassword(request);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authenticationService.resendVerification(request.getEmail());
            return ResponseEntity.ok("Verification email sent.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.ok(
                "If that email is registered you will receive a verification email.");
        }
    }
}
