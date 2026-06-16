package com.restomgmt.site.user;

import java.util.*;
//import jakarta.persistence.*;

//import org.springframework.beans.factory.annotation.Autowired;

import com.restomgmt.site.user.models.UserNew;
import com.restomgmt.site.user.models.RoleNew;
import com.restomgmt.site.user.repositories.UserNewRepository;
import com.restomgmt.site.user.permission.Permission;

import io.swagger.v3.oas.annotations.Parameter;

import com.restomgmt.site.user.repositories.RoleNewRepository;
import com.restomgmt.site.user.repositories.PermissionRepository;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.transaction.Transactional;

//this does the setup
//creates the permissions, then the roles, assign permissions to roles, then create users and assign a role to them
//alreadySetup flag first checks if the setup needs to be run

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    boolean alreadySetup = false;

    //parameter instead of autowired is better for testing, makes dependencies clearer

    //@Autowired
    @Parameter
    private UserNewRepository userRepository;

    //@Autowired
    @Parameter
    private RoleNewRepository roleRepository;

    //@Autowired 
    @Parameter
    private PermissionRepository permissionRepository;

    //@Autowired 
    @Parameter
    private PasswordEncoder passwordEncoder;

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
        createRoleIfNotFound("ROLE_ADMIN", adminPermissions);
        createRoleIfNotFound("ROLE_USER", Arrays.asList(readPermission));

        RoleNew adminRole = roleRepository.findByName("ROLE_ADMIN");
        UserNew user = new UserNew();
        user.setFullName("Test Tester");
        user.setPassword(passwordEncoder.encode("testpassword"));
        user.setEmail("test@test.com");
        user.setEnabled(true);
        user.setToken(false);
        user.setUsername("testUsername");
    }

    @Transactional
    Permission createPermissionIfNotFound(String name) {
        Permission permission = permissionRepository.findByName(name);
        if(permission == null) {
            permission = new Permission(name);
            permissionRepository.save(permission);
        }
        return permission;
    }

    @Transactional
    RoleNew createRoleIfNotFound(
        String name, Collection<Permission> permissions) {

            RoleNew role = roleRepository.findByName(name);
            if(role == null) {
                role = new RoleNew(name);
                role.setPermissions(permissions);
                roleRepository.save(role);
            }
            return role;
        }
    
}
