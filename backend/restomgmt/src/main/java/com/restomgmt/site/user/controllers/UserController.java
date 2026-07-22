package com.restomgmt.site.user.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.restomgmt.site.user.dto.RoleAssignmentRequest;
import com.restomgmt.site.user.dto.UserResponse;
import com.restomgmt.site.user.dto.UserUpdateRequest;
import com.restomgmt.site.user.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users/")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    //fetch all students with endpoint
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /users/ - fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("GET /users/{} - fetching user by id", id);
        return userService.findUserById(id)
                          .map(ResponseEntity::ok)
                          .orElseGet(() -> {
                              log.warn("User id {} not found", id);
                              return ResponseEntity.notFound().build();
                          });
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            log.info("DELETE /users/{}/delete - deleting user", id);
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Attempt to delete missing user id {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request){
        try {
            log.info("PUT /users/{}/update - updating user", id);
            return ResponseEntity.ok(userService.updateUser(id, request));
        } catch (NoSuchElementException e) {
            log.warn("Attempt to update missing user id {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Access denied: insufficient permissions");
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long id,
            @RequestBody RoleAssignmentRequest request) {
        try {
            return ResponseEntity.ok(userService.assignRole(id, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

}