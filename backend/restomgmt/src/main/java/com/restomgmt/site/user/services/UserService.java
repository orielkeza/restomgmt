package com.restomgmt.site.user.services;

import com.restomgmt.site.user.dto.AdminCreateUserRequest;
import com.restomgmt.site.user.dto.RoleAssignmentRequest;
import com.restomgmt.site.user.dto.UserResponse;
import com.restomgmt.site.user.dto.UserUpdateRequest;
import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public User addUser (User user) {
        log.info("Adding user {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll()
                             .stream()
                             .map(this::toResponse)
                             .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findUserById(Long id) {
        log.debug("Finding user by id {}", id);
        return userRepository.findById(id)
                             .map(this::toResponse);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        userRepository.delete(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user id={}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        return toResponse(userRepository.save(user));
    }

    private static final Set<String> SUPPORTED_ROLES = 
        Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_STAFF");

    public UserResponse assignRole(Long userId, RoleAssignmentRequest request) {
        String roleName = request.getRoleName().toUpperCase();
        
        if (!SUPPORTED_ROLES.contains(roleName)) {
            throw new IllegalArgumentException(
                "Unsupported role: " + roleName + 
                ". Supported roles are: ROLE_USER, ROLE_ADMIN, ROLE_STAFF");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NoSuchElementException("Role not found: " + roleName));
        if (role == null) {
            throw new NoSuchElementException("Role not found: " + roleName);
        }

        Collection<Role> roles = new ArrayList<>(user.getRoles());
        if (!roles.stream().anyMatch(r -> r.getName().equals(roleName))) {
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);
        }

        return toResponse(user);
    }

    public UserResponse removeRole(Long userId, RoleAssignmentRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        Collection<Role> roles = new ArrayList<>(user.getRoles());
        roles.removeIf(r -> r.getName().equals(request.getRoleName()));
        user.setRoles(roles);
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public UserResponse adminCreateUser(AdminCreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setEnabled(true); // admin created users are pre-verified
        user.setTokenExpired(false);

        String roleName = request.getRoleName() != null ? 
            request.getRoleName() : "ROLE_USER";
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NoSuchElementException("Role not found: " + roleName));
        if (role != null) {
            user.setRoles(new ArrayList<>(List.of(role)));
        }

        userRepository.save(user);

        // Send email with credentials
        emailService.sendAdminCreatedAccountEmail(
            user.getEmail(), user.getUsername(), temporaryPassword);

        log.info("Admin created user: {}", user.getUsername());
        return toResponse(user);
    }

    private String generateTemporaryPassword() {
        // Generates a password that meets constraints:
        // uppercase, number, special char, 12 chars long
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + numbers + special;

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();

        // Guarantee at least one of each required type
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fill remaining 9 chars from all
        for (int i = 0; i < 9; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle so required chars aren't always at the start
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .enabled(user.getEnabled())
            .build();
    }
}