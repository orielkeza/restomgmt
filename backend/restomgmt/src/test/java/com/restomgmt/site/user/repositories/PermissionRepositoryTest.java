package com.restomgmt.site.user.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.restomgmt.site.user.models.Permission;
import com.restomgmt.site.user.models.Role;

@DataJpaTest
@ActiveProfiles("uat")
class PermissionRepositoryTest {
    
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role role;
    private Permission permission;

    @BeforeEach
    void setUp() {
        permissionRepository.deleteAll();
        roleRepository.deleteAll();

        role = new Role();
        role.setName("ROLE_USER");

        roleRepository.save(role);

        permission = new Permission();
        permission.setName("Ordering");

    
        permissionRepository.save(permission);
    }

    @Test
    void findByNameShouldReturnCorrectUserWhenUsernameIsCorrect() {
        Optional<Permission> result = permissionRepository.findByName("Ordering");
        
        assertEquals("Ordering", result.get().getName());
    }

    @Test
    void findByNameShouldReturnEmptyWhenUsernameIsIncorrect() {
        Optional<Permission> permission = permissionRepository.findByName("False");

        assertFalse(permission.isPresent());
        assertTrue(permission.isEmpty());
    }
}
