package com.restomgmt.site.user.controllers;

import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.security.AuthenticationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) {
        String token = authenticationService.authenticate(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register") 
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(authenticationService.registerUser(user));
    }
}
