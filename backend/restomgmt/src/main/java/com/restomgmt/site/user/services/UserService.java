package com.restomgmt.site.user.services;

import com.restomgmt.site.user.dto.UserResponse;
import com.restomgmt.site.user.dto.UserUpdateRequest;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public User addUser (User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                             .stream()
                             .map(this::toResponse)
                             .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findUserById(Long id) {
        return userRepository.findById(id)
                             .map(this::toResponse);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        userRepository.delete(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        return toResponse(userRepository.save(user));
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