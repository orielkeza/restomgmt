package com.restomgmt.site;

import com.restomgmt.site.user.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.*;
//import jakarta.persistence.*;

//import org.springframework.beans.factory.annotation.Autowired;

import com.restomgmt.site.user.models.User;
import com.restomgmt.site.menu.models.Category;
import com.restomgmt.site.menu.models.MenuItem;
import com.restomgmt.site.user.models.Permission;
import com.restomgmt.site.user.models.Role;

import com.restomgmt.site.menu.repositories.CategoryRepository;
import com.restomgmt.site.menu.repositories.MenuItemRepository;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.PermissionRepository;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
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
@Profile("!uat")
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final CategoryRepository categoryRepository;

    private final MenuItemRepository menuItemRepository;

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

        Category mainsCategory = createCategoryIfNotFound("Mains");
        Category drinksCategory = createCategoryIfNotFound("Drinks");
        Category dessertsCategory = createCategoryIfNotFound("Desserts");

        createMenuItemIfNotFound("Grilled Chicken", "Herb-marinated grilled chicken", new BigDecimal("12.99"), mainsCategory);
        createMenuItemIfNotFound("Chocolate Cake", "Rich dark chocolate cake", new BigDecimal("5.99"), dessertsCategory);
        createMenuItemIfNotFound("Lemonade", "Fresh squeezed lemonade", new BigDecimal("2.99"), drinksCategory);

        Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
        if(userRepository.findByUsername("testUsername").isEmpty()) {
            User user = new User();
            user.setFullName("Test Tester");
            user.setPassword(passwordEncoder.encode("testpassword"));
            user.setEmail("test@test.com");
            user.setEnabled(true);
            user.setTokenExpired(false);
            user.setUsername("testUsername");
            user.setRoles(adminRole.map(List::of).orElseGet(List::of));
            userRepository.save(user);
        }

        alreadySetup=true;
    }

    @Transactional
    Permission createPermissionIfNotFound(String name) {
        return permissionRepository.findByName(name)
            .orElseGet(() -> {
                Permission permission = Permission.builder().name(name).build();
                return permissionRepository.save(permission);
            });
    }

    @Transactional
    Role createRoleIfNotFound(String name, Collection<Permission> permissions, Collection<User> users) {
        return roleRepository.findByName(name)
            .orElseGet(() -> {
                Role role = Role.builder().name(name).permissions(permissions).build();
                return roleRepository.save(role);
            });
    }

    @Transactional
    Category createCategoryIfNotFound(String name) {
        return categoryRepository.findByName(name)
            .orElseGet(() -> {
                Category category = Category.builder().name(name).build();
                return categoryRepository.save(category);
            });
    }

    @Transactional
    void createMenuItemIfNotFound(String name, String description, BigDecimal cost, Category category) {
        if (menuItemRepository.findAll().stream().noneMatch(i -> i.getName().equals(name))) {
        MenuItem item = MenuItem.builder()
            .name(name)
            .description(description)
            .cost(cost)
            .available(true)
            .category(category)
            .build();
        menuItemRepository.save(item);
    }
    }
    
}
