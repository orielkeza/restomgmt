package com.restomgmt.site.user;

import com.restomgmt.site.user.repositories.UserRepository;
import java.util.*;
//import jakarta.persistence.*;

//import org.springframework.beans.factory.annotation.Autowired;

import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.models.Permission;
import com.restomgmt.site.user.models.Role;

import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.PermissionRepository;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

//this does the setup
//creates the permissions, then the roles, assign permissions to roles, then create users and assign a role to them
//alreadySetup flag first checks if the setup needs to be run

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(alreadySetup) {
            return;
        }
        Permission readPermission 
         = createPermissionIfNotFound ("READ_PERMISSION");
        Permission writePermission
            = createPermissionIfNotFound("WRITE_PERMISSION");
        
        List<Permission> adminPermissions = Arrays.asList(
            readPermission, writePermission
        );
        createRoleIfNotFound("ROLE_ADMIN", adminPermissions, null);
        createRoleIfNotFound("ROLE_USER", Arrays.asList(readPermission), null);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if(userRepository.findByUsername("testUsername").isEmpty()) {
            User user = new User();
            user.setFullName("Test Tester");
            user.setPassword(passwordEncoder.encode("testpassword"));
            user.setEmail("test@test.com");
            user.setEnabled(true);
            user.setTokenExpired(false);
            user.setUsername("testUsername");
            user.setRoles(List.of(adminRole));
            userRepository.save(user);
        }

        alreadySetup=true;
    }

    @Transactional
    Permission createPermissionIfNotFound(String name) {
        Permission permission = permissionRepository.findByName(name);
        if(permission == null) {
            permission = new Permission();
            permission.setName(name);
            permissionRepository.save(permission);
        }
        return permission;
    }

    @Transactional
    Role createRoleIfNotFound(
        String name, Collection<Permission> permissions, Collection<User> users) {

            Role role = roleRepository.findByName(name);
            if(role == null) {
                role = new Role();
                role.setName(name);
                role.setPermissions(permissions);
                roleRepository.save(role);
            }
            return role;
        }
    
}
