package com.restomgmt.site.user.controllers;

import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.security.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) {
        try {
            log.info("POST /api/auth/login - authentication attempt for user={}", user.getUsername());
            String token = authenticationService.authenticate(user.getUsername(), user.getPassword());
            log.info("Authentication successful for user={}", user.getUsername());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user={}", user.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register") 
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        //this is a no go because this returns a raw user including the hashed password which is a risk
        //return ResponseEntity.ok(authenticationService.registerUser(user));
        log.info("POST /api/auth/register - registering user={}", user.getUsername());
        authenticationService.registerUser(user);
        log.info("User registered: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
