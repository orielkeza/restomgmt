package com.restomgmt.site.user.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
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
        String username = "testUsername";

        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsernameShouldReturnNonNullCorrectUsername() {
        String username = "testUsername";

        String token = jwtUtil.generateToken(username);

        assertNotNull(jwtUtil.extractUsername(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void extractExpirationShouldReturnFutureExpirationDate() {
        String username = "testUsername";

        String token = jwtUtil.generateToken(username);

        Date futureDateTest = new Date(System.currentTimeMillis());

        Date testDate = jwtUtil.extractExpiration(token);

        assertNotEquals(testDate, futureDateTest);
        assertTrue(testDate.after(futureDateTest));
    }

    @Test
    void validateTokenShouldReturnCorrectBoolean (){
        String username = "testUsername";

        String token = jwtUtil.generateToken(username);

        Boolean isValid = jwtUtil.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    void validateTokenShouldReturnCorrectBooleanWhenUsernamesDoNotMatch (){
        String username = "testUsername";

        String token = jwtUtil.generateToken(username);

        String wrongUsername = "wrongUsername";

        Boolean isValid = jwtUtil.validateToken(token, wrongUsername);

        assertFalse(isValid);
    }
}