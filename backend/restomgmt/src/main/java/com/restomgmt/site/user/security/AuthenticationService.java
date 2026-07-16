package com.restomgmt.site.user.security;

import com.restomgmt.site.user.dto.RegisterRequest;
import com.restomgmt.site.user.dto.ResetPasswordRequest;
import com.restomgmt.site.user.models.EmailVerificationToken;
import com.restomgmt.site.user.models.PasswordResetToken;
import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.EmailVerificationTokenRepository;
import com.restomgmt.site.user.repositories.PasswordResetTokenRepository;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.services.EmailService;
import com.restomgmt.site.user.util.JwtUtil;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationService implements UserDetailsService {
    
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtUtil jwtUtil;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final EmailService emailService;

    @Lazy
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository,
                                        JwtUtil jwtUtil,
                                        @Lazy AuthenticationManager authenticationManager,
                                        PasswordEncoder passwordEncoder,
                                        EmailVerificationTokenRepository emailVerificationTokenRepository,
                                        PasswordResetTokenRepository passwordResetTokenRepository,
                                        EmailService emailService){                   
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
        this.jwtUtil=jwtUtil;
        this.authenticationManager=authenticationManager;
        this.passwordEncoder=passwordEncoder;
        this.emailVerificationTokenRepository=emailVerificationTokenRepository;
        this.passwordResetTokenRepository=passwordResetTokenRepository;
        this.emailService=emailService;
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

        if (!user.getEnabled()) {
            throw new DisabledException("Email not verified. Please check your inbox.");
        }

        log.debug("Roles for {}: {}", username, user.getRoles());

        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(r -> new SimpleGrantedAuthority(r.getName()))
            .collect(Collectors.toList());

        log.debug("Authorities: {}", authorities);

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }

    @Transactional
    public void register(RegisterRequest request) {
        // Check username not already taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Check email not already taken
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check username not in password
        if (request.getPassword().toLowerCase()
                .contains(request.getUsername().toLowerCase())) {
            throw new IllegalArgumentException("Password must not contain username");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false); // disabled until email verified
        user.setTokenExpired(false);

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new NoSuchElementException("ROLE_USER not found"));
        if (userRole != null) {
            user.setRoles(new ArrayList<>(List.of(userRole)));
        }

        userRepository.save(user);

        // Generate and send verification token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
            .token(token)
            .user(user)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .used(false)
            .build();
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
        log.info("User registered: {}", user.getUsername());
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken =
            emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getUsername());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        // Don't reveal if email exists for security
        userRepository.findByEmail(email).ifPresent(user -> {
            // Delete any existing token
            passwordResetTokenRepository.deleteByUser_Id(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
            log.info("Password reset requested for: {}", email);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken =
            passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }

        User user = resetToken.getUser();

        // Check username not in new password
        if (request.getNewPassword().toLowerCase()
                .contains(user.getUsername().toLowerCase())) {
            throw new IllegalArgumentException("Password must not contain username");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset for user: {}", user.getUsername());
    }

    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Email not found"));

        if (user.getEnabled()) {
            throw new IllegalStateException("Email already verified");
        }

        emailVerificationTokenRepository.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
            .token(token)
            .user(user)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .used(false)
            .build();
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
    }
}