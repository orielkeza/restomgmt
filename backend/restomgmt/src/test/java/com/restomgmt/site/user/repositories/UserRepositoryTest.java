package com.restomgmt.site.user.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;

@DataJpaTest
@ActiveProfiles("uat")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setName("ROLE_USER");

        roleRepository.save(role);

        user = User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .password("encodedPass123!")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .roles(List.of(role))
                        .build();

        userRepository.save(user);
    }

    @Test
    void findByUsernameShouldReturnCorrectUserWhenUsernameIsCorrect() {
        Optional<User> result = userRepository.findByUsername("theJohnD");
        assertTrue(result.isPresent());
        assertFalse(result.isEmpty());
        assertEquals("theJohnD", result.get().getUsername());
    }

    @Test
    void findByUsernameShouldReturnNullWhenUsernameIsIncorrect() {
        Optional<User> user = userRepository.findByUsername("falseUser");
        
        assertFalse(user.isPresent());
        assertTrue(user.isEmpty());
    }

    @Test
    void findByEmailShouldReturnCorrectUserWhenEmailIsCorrect() {
        
    }

    @Test
    void findByEmailShouldReturnNullWhenEmailIsCorrect() {
        
    }
}