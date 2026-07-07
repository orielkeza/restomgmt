package com.restomgmt.site.user.controllers;

import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.security.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user) {
        try {
            String token = authenticationService.authenticate(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register") 
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        //this is a no go because this returns a raw user including the hashed password which is a risk
        //return ResponseEntity.ok(authenticationService.registerUser(user));
        authenticationService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
