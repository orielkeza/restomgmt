package com.restomgmt.site.user.security;

import com.restomgmt.site.user.models.User;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationService implements UserDetailsService {
    
    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    @Lazy
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository,
                                        JwtUtil jwtUtil,
                                        @Lazy AuthenticationManager authenticationManager,
                                        PasswordEncoder passwordEncoder){                   
        this.userRepository=userRepository;
        this.jwtUtil=jwtUtil;
        this.authenticationManager=authenticationManager;
        this.passwordEncoder=passwordEncoder;
    }

    public String authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final UserDetails userDetails = loadUserByUsername(username);
        return jwtUtil.generateToken(userDetails.getUsername());    
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<GrantedAuthority> authorities = user.getRoles().stream()
                                                 .map(r -> new SimpleGrantedAuthority(r.getName()))
                                                 .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                                                                      user.getPassword(),
                                                                      authorities);
    }

    public User registerUser (User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}