package com.restomgmt.site.user.services;

import com.restomgmt.site.user.dto.RoleAssignmentRequest;
import com.restomgmt.site.user.dto.UserResponse;
import com.restomgmt.site.user.dto.UserUpdateRequest;
import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
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

    public UserResponse assignRole(Long userId, RoleAssignmentRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        Role role = roleRepository.findByName(request.getRoleName())
            .orElseThrow(() -> new NoSuchElementException("Role not found: " + request.getRoleName()));

        Collection<Role> roles = new ArrayList<>(user.getRoles());
        if (!roles.contains(role)) {
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);
        }

        return toResponse(user);
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