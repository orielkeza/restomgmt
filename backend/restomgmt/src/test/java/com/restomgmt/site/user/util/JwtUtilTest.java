package com.restomgmt.site.user.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;

    private final List<GrantedAuthority> testAuthorities = 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    private final String username = "testUsername";

    //BeforeEach runs before each test method
    //no spring context neeed here to inject JwtUtil test vals for the @Value fields

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secreet-key-minimum-32-characters!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
    }

    @Test
    void generateTokenShouldReturnNonNullToken() {
        // Pass the authorities to the updated method
        String token = jwtUtil.generateToken(username, testAuthorities);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsernameShouldReturnNonNullCorrectUsername() {
        String token = jwtUtil.generateToken(username, testAuthorities);

        assertNotNull(jwtUtil.extractUsername(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void extractExpirationShouldReturnFutureExpirationDate() {
        String token = jwtUtil.generateToken(username, testAuthorities);

        Date futureDateTest = new Date(System.currentTimeMillis());
        Date testDate = jwtUtil.extractExpiration(token);

        assertNotEquals(testDate, futureDateTest);
        assertTrue(testDate.after(futureDateTest));
    }

    @Test
    void validateTokenShouldReturnCorrectBoolean() {
        String token = jwtUtil.generateToken(username, testAuthorities);

        Boolean isValid = jwtUtil.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    void validateTokenShouldReturnCorrectBooleanWhenUsernamesDoNotMatch() {
        String token = jwtUtil.generateToken(username, testAuthorities);
        String wrongUsername = "wrongUsername";

        Boolean isValid = jwtUtil.validateToken(token, wrongUsername);

        assertFalse(isValid);
    }

    @Test
    void extractRolesShouldReturnEmbeddedAuthorities() {
        List<GrantedAuthority> customAuthorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        String token = jwtUtil.generateToken(username, customAuthorities);
        
        List<GrantedAuthority> extractedAuthorities = jwtUtil.extractRoles(token);
        
        assertNotNull(extractedAuthorities);
        assertEquals(2, extractedAuthorities.size());
        assertTrue(extractedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}