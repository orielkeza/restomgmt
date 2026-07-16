package com.restomgmt.site.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.restomgmt.site.user.dto.RegisterRequest;
import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.EmailVerificationTokenRepository;
import com.restomgmt.site.user.repositories.PasswordResetTokenRepository;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.services.EmailService;
import com.restomgmt.site.user.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName("ROLE_USER");

        user = User.builder()
            .email("john.doe@gmail.com")
            .enabled(true)
            .fullName("John Doe")
            .password("encodedPass123!")
            .tokenExpired(false)
            .username("theJohnD")
            .roles(List.of(userRole))
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    // authenticate
    @Test
    void authenticateShouldReturnTokenWhenCredentialsAreValid() {
        when(userRepository.findByUsername("theJohnD")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq("theJohnD"), anyCollection()))
            .thenReturn("mock-jwt-token");

        String token = authenticationService.authenticate("theJohnD", "password");

        assertEquals("mock-jwt-token", token);
        verify(authenticationManager).authenticate(
            any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticateShouldThrowWhenCredentialsAreInvalid() {
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class,
            () -> authenticationService.authenticate("theJohnD", "wrongpassword"));
    }

    @Test
    void authenticateShouldThrowWhenCredentialsAreNull() {
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class,
            () -> authenticationService.authenticate(null, null));
    }

    // loadUserByUsername
    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        when(userRepository.findByUsername("theJohnD")).thenReturn(Optional.of(user));

        UserDetails result = authenticationService.loadUserByUsername("theJohnD");

        assertEquals("theJohnD", result.getUsername());
        assertEquals("encodedPass123!", result.getPassword());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> authenticationService.loadUserByUsername("ghost"));
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserNotVerified() {
        user.setEnabled(false);
        when(userRepository.findByUsername("theJohnD")).thenReturn(Optional.of(user));

        assertThrows(DisabledException.class,
            () -> authenticationService.loadUserByUsername("theJohnD"));
    }

    // register
    @Test
    void registerShouldSaveUserWithEncodedPasswordAndDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setFullName("New User");
        request.setPassword("Password123!");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(any(), any(), any());

        authenticationService.register(request);

        // Verify user was saved with encoded password and disabled until verified
        verify(userRepository).save(argThat(savedUser ->
            savedUser.getUsername().equals("newuser") &&
            savedUser.getPassword().equals("encodedPassword") &&
            !savedUser.getEnabled() &&
            savedUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_USER"))
        ));
        verify(emailService).sendVerificationEmail(eq("new@test.com"), eq("newuser"), any());
    }

    @Test
    void registerShouldThrowWhenUsernameAlreadyTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("theJohnD");
        request.setEmail("other@test.com");
        request.setPassword("Password123!");

        when(userRepository.findByUsername("theJohnD")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
            () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("john.doe@gmail.com");
        request.setPassword("Password123!");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john.doe@gmail.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
            () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowWhenPasswordContainsUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("newuserPassword123!");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }
}
