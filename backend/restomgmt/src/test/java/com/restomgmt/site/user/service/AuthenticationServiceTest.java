package com.restomgmt.site.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");

        user = User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .password("encodedPass123!")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .roles(List.of(role))
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        assertEquals(1L, user.getId());
    }

    @Test
    public void authenticateShouldReturnTokenWhenCredentialsAreValid() {
        assertNotNull(user);
        when(userRepository.findByUsername("theJohnD"))
            .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("theJohnD"))
            .thenReturn("mock-jwt-token");

        String token = authenticationService.authenticate("theJohnD", "password");

        assertEquals("mock-jwt-token", token);
        verify(authenticationManager).authenticate(
            any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    public void authenticateShouldThrowTokenWhenCredentialsAreNotValid() {
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager)
            .authenticate(any());

        assertThrows(BadCredentialsException.class, 
            () -> authenticationService.authenticate("theJohnD", null)
        );
    }

        @Test
    public void authenticateShouldThrowTokenWhenCredentialsAreNull() {
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager)
            .authenticate(any());

        assertThrows(BadCredentialsException.class, 
            () -> authenticationService.authenticate(null, null)
        );
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserIsValid() {
        when(userRepository.findByUsername("theJohnD"))
            .thenReturn(Optional.of(user));

        UserDetails result = authenticationService.loadUserByUsername("theJohnD");

        assertEquals("theJohnD", result.getUsername());
        assertEquals("encodedPass123!", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                         .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void loadByUsernameShouldThrowWhenUserIsNotValid() {
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                     () -> authenticationService.loadUserByUsername("nonexistent")
        );
    }

    @Test
    public void registerShouldEncodePasswordAndSaveUser() {
        assertNotNull(user);
        when(passwordEncoder.encode("apassword"))
             .thenReturn("encodedPass123!");
        when(userRepository.save(any(User.class)))
             .thenReturn(user);

        User newUser = User.builder()
                        .password("apassword")
                        .username("newuser")
                        .build();
        ReflectionTestUtils.setField(newUser, "id", 1L);
                
        assertEquals(1L, newUser.getId());

        authenticationService.registerUser(newUser);

        assertEquals("encodedPass123!", newUser.getPassword());
        verify(userRepository).save(newUser);
    }
}
