package com.restomgmt.site.user.controllers;

import com.restomgmt.site.user.models.UserNew;
import com.restomgmt.site.user.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    
    @Autowired 
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserNew user) {
        String token = authenticationService.authenticate(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register") 
    public ResponseEntity<?> registerUser(@RequestBody UserNew user) {
        return ResponseEntity.ok(authenticationService.registerUser(user));
    }
}
