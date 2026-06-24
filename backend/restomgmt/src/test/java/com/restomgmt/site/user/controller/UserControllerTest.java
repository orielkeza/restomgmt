package com.restomgmt.site.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.services.UserService;

import lombok.RequiredArgsConstructor;

@WebMvcTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class UserControllerTest {
    
    private final MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    private Role adminRole;
    private Role staffRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        staffRole = new Role();
        staffRole.setName("ROLE_STAFF");
        userRole = new Role();
        userRole.setName("ROLE_USER");
    }


    @Test
    public void testGetUserById () throws Exception {
        User user = User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .password("*jjehcH.03")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .roles(List.of(adminRole))
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);
                
        assertEquals(1L, user.getId());

        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.get("/1"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value("John Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.password").value("*jjehcH.03"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("theJohnD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.enabled").value("true"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.tokenExpired").value("false"));
        verify(userService).addUser(any());
    }

    @Test 
    public void testGetAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        
        users.add(User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .password("*jjehcH.03")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .roles(List.of(adminRole))
                        .build());
        ReflectionTestUtils.setField(users.get(0), "id", 1L);
                
        assertEquals(1L, users.get(0).getId());

        users.add(User.builder()
                        .email("jane.doe@gmail.com")
                        .enabled(true)
                        .fullName("Jane Doe")
                        .password("*jjKKcH.03")
                        .tokenExpired(false)
                        .username("JaneD")
                        .roles(List.of(adminRole))
                        .build());
        ReflectionTestUtils.setField(users.get(1), "id", 2L);
                
        assertEquals(2L, users.get(1).getId());

        when(userService.findUserById(1L)).thenReturn(Optional.of(users.get(0)));
        mockMvc.perform(MockMvcRequestBuilders.get("/users/"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("jane.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].fullName").value("Jane Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].password").value("*jjKKcH.03"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].username").value("JaneD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].enabled").value("true"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].tokenExpired").value("false"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("john.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].fullName").value("John Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].password").value("*jjehcH.03"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("theJohnD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].enabled").value("true"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].tokenExpired").value("false"));
    }

    @Test
    public void testDeleteUser() {
        User user = User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .password("*jjehcH.03")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .roles(List.of(adminRole))
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);
                
        assertEquals(1L, user.getId());

        doNothing().when(userService).deleteUser(user);
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.delete("/1L"))
        .andExpect(MockMvcResultMatchers.status().isOk());
        verify(userService).deleteUser(user);
    }

    @Test
    public void testUpdateUser() {

        User user = User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .password("*jjehcH.03")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .roles(List.of(adminRole))
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);
                
        assertEquals(1L, user.getId());

        when(userService.updateUser(user)).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.put("/1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value("John Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.password").value("*jjehcH.03"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("theJohnD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.enabled").value("true"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.tokenExpired").value("false"));
    }
}
