package com.restomgmt.site.user.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;

@DataJpaTest
@ActiveProfiles("uat")
class RoleRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        role = new Role();
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
    void findByNameShouldReturnCorrectUserWhenNameIsCorrect() {
        Optional<Role> result = roleRepository.findByName("ROLE_USER");
        
        assertEquals("ROLE_USER", result.get().getName());
    }

    @Test
    void findByNameShouldReturnEmptyWhenNameIsIncorrect() {
        Optional<Role> role = roleRepository.findByName("ROLE_FALSE");

        assertFalse(role.isPresent());
        assertTrue(role.isEmpty());
    }
}
