package com.restomgmt.site.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.services.UserService;
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

        User user = User.builder()
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
    public void testAuthenticate() {

    }

    @Test
    public void testRgister() {

    }

    @Test 
    public void testLoadUserByUsername() {
        
    }
}
