package com.restomgmt.site.user.security;

import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.util.JwtUtil;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationService implements UserDetailsService {
    
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtUtil jwtUtil;

    @Lazy
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository,
                                        JwtUtil jwtUtil,
                                        @Lazy AuthenticationManager authenticationManager,
                                        PasswordEncoder passwordEncoder){                   
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
        this.jwtUtil=jwtUtil;
        this.authenticationManager=authenticationManager;
        this.passwordEncoder=passwordEncoder;
    }

    public String authenticate(String username, String password) {
        log.info("Authenticating user={}", username);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final UserDetails userDetails = loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
        log.debug("Generated JWT for user={}", username);
        return token;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.debug("Roles for {}: {}", username, user.getRoles());
        List<GrantedAuthority> authorities = user.getRoles().stream()
                                                 .map(r -> new SimpleGrantedAuthority(r.getName()))
                                                 .collect(Collectors.toList());
        log.debug("Authorities: {}", authorities);
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                                                                      user.getPassword(),
                                                                      authorities);
    }

    public User registerUser (User user) {
        log.info("Registering user={}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER is not configured"));
        user.setRoles(new ArrayList<>(List.of(userRole)));
        User saved = userRepository.save(user);
        log.debug("User persisted id={}", saved.getId());
        return saved;
    }
}