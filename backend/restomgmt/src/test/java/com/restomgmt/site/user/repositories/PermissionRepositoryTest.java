package com.restomgmt.site.user.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        Permission result = permissionRepository.findByName("Ordering");
        
        assertEquals("Ordering", result.getName());
    }

    @Test
    void findByNameShouldReturnNullWhenUsernameIsIncorrect() {
        Permission permission = permissionRepository.findByName("False");
        
        assertNull(permission);
    }
}
