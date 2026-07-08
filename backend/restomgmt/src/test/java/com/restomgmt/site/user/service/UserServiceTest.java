package com.restomgmt.site.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restomgmt.site.user.dto.UserResponse;
import com.restomgmt.site.user.dto.UserUpdateRequest;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.services.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .username("theJohnD")
            .email("john.doe@gmail.com")
            .fullName("John Doe")
            .password("encodedpassword")
            .enabled(true)
            .tokenExpired(false)
            .build();
    }

    // getAllUsers
    @Test
    void getAllUsersShouldReturnListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("theJohnD", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsersShouldReturnEmptyListWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    // findUserById
    @Test
    void findUserByIdShouldReturnUserResponseWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserResponse> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("theJohnD", result.get().getUsername());
    }

    @Test
    void findUserByIdShouldReturnEmptyWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserResponse> result = userService.findUserById(99L);

        assertFalse(result.isPresent());
    }

    // deleteUser
    @Test
    void deleteUserShouldDeleteWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).delete(any());
    }

    // updateUser
    @Test
    void updateUserShouldReturnUpdatedUserResponse() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.updateUser(1L, request);

        assertEquals("jane@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldThrowWhenUserDoesNotExist() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.updateUser(99L, request));
        verify(userRepository, never()).save(any());
    }

    // addUser
    @Test
    void addUserShouldSaveAndReturnUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.addUser(user);

        assertNotNull(result);
        assertEquals("theJohnD", result.getUsername());
        verify(userRepository).save(user);
    }
}
