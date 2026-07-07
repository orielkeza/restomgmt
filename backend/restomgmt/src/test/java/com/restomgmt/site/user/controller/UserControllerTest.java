package com.restomgmt.site.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.restomgmt.site.user.controllers.AuthenticationController;
import com.restomgmt.site.user.controllers.UserController;
import com.restomgmt.site.user.dto.UserResponse;
import com.restomgmt.site.user.dto.UserUpdateRequest;
import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.services.UserService;
import com.restomgmt.site.user.util.JwtUtil;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
    })
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
})
@ActiveProfiles("uat")
class UserControllerTest {

    @SpringBootConfiguration
    @Import(UserController.class)
    static class TestConfig {
    }

    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private com.restomgmt.site.user.repositories.UserRepository userRepository;

    @MockitoBean
    private com.restomgmt.site.user.repositories.RoleRepository roleRepository;

    @MockitoBean
    private com.restomgmt.site.user.repositories.PermissionRepository permissionRepository;

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
        UserResponse user = UserResponse.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        //.password("*jjehcH.03")
                        //.tokenExpired(false)
                        .username("theJohnD")
                        //.roles(List.of(adminRole))
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
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("theJohnD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.enabled").value(true));
        verify(userService).findUserById(1L);
    }

    @Test 
    public void testGetAllUsers() throws Exception {
        List<UserResponse> users = new ArrayList<>();
        
        users.add(UserResponse.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        //.password("*jjehcH.03")
                        //.tokenExpired(false)
                        .username("theJohnD")
                        //.roles(List.of(adminRole))
                        .build());
        ReflectionTestUtils.setField(users.get(0), "id", 1L);
                
        assertEquals(1L, users.get(0).getId());

        users.add(UserResponse.builder()
                        .email("jane.doe@gmail.com")
                        .enabled(true)
                        .fullName("Jane Doe")
                        //.password("*jjKKcH.03")
                        //.tokenExpired(false)
                        .username("JaneD")
                        //.roles(List.of(adminRole))
                        .build());
        ReflectionTestUtils.setField(users.get(1), "id", 2L);
                
        assertEquals(2L, users.get(1).getId());

        when(userService.getAllUsers()).thenReturn(users);
        mockMvc.perform(MockMvcRequestBuilders.get("/users/"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("jane.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].fullName").value("Jane Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].username").value("JaneD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].enabled").value(true))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("john.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].fullName").value("John Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("theJohnD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].enabled").value(true));
    }

    @Test
    public void testDeleteUser () throws Exception {
        UserResponse user = UserResponse.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        //.password("*jjehcH.03")
                        //.tokenExpired(false)
                        .username("theJohnD")
                        //.roles(List.of(adminRole))
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);
                
        assertEquals(1L, user.getId());

        doNothing().when(userService).deleteUser(1L);
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.delete("/1"))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
        verify(userService).deleteUser(1L);
    }


    @Test
    public void testUpdateUser() throws Exception {
        UserResponse user = UserResponse.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .username("theJohnD")
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);
                
        assertEquals(1L, user.getId());
        
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
            .thenReturn(UserResponse.builder()
            .id(1L)
            .username("theJohnD")
            .email("john.doe@gmail.com")
            .fullName("John Doe")
            .enabled(true)
            .build());
        mockMvc.perform(MockMvcRequestBuilders.put("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"fullName\":\"John Doe\",\"email\":\"john.doe@gmail.com\"}"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@gmail.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value("John Doe"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("theJohnD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.enabled").value(true));
    }
}
