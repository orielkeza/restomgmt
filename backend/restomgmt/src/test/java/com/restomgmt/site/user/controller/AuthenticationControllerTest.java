package com.restomgmt.site.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.restomgmt.site.user.controllers.AuthenticationController;
import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.util.JwtUtil;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

@WebMvcTest(
    controllers = AuthenticationController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
    })
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
}) //it's needed for the repos but here it oulls it into a web-slice test and since the controller test doesn't have a db, it's looking for things that are not there
@ActiveProfiles("uat")
class AuthenticationControllerTest {

    @SpringBootConfiguration
    @Import(AuthenticationController.class)
    static class TestConfig {
    }


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;


    @Test
    public void testLoginShouldReturnTokenWhenCredentialsAreValid() throws Exception{
        when(authenticationService.authenticate("theJohnD", "password123!"))
            .thenReturn("mocked-jwt-token");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"password123!\"}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("mocked-jwt-token"));

        verify(authenticationService).authenticate("theJohnD", "password123!");
    }

    @Test
    public void testLoginShouldNotReturn401WhenPasswordIsWrong() throws Exception{
        when(authenticationService.authenticate("theJohnD", "wrongPassword123!"))
            .thenThrow(new BadCredentialsException("Wrong password"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"wrongPassword123!\"}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void testLoginShouldNotReturn401WhenUsernameIsInvalid() throws Exception{
        when(authenticationService.authenticate("wrongUsername", "password123!"))
            .thenThrow(new BadCredentialsException("Invalid username"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wrongUsername\",\"password\":\"password123!\"}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void testLoginShouldNotReturn401WhenPasswordIsNull() throws Exception{
        when(authenticationService.authenticate("theJohnD",null))
            .thenThrow(new BadCredentialsException("No password entered"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":null}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void testLoginShouldNotReturn401WhenUsernameIsNull() throws Exception{
        when(authenticationService.authenticate(null,"password123!"))
            .thenThrow(new BadCredentialsException("No username entered"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":null,\"password\":\"password123!\"}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void testLoginShouldNotReturn401WhenUsernameAndPasswordAreNull() throws Exception{
        when(authenticationService.authenticate(null,null))
            .thenThrow(new BadCredentialsException("No credentials entered"));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":null,\"password\":null}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void testRegisterShouldReturn201WhenCredentialsAreValid() throws Exception {
        User user = User.builder()
                        .email("john.doe@gmail.com")
                        .enabled(true)
                        .fullName("John Doe")
                        .tokenExpired(false)
                        .username("theJohnD")
                        .build();
        ReflectionTestUtils.setField(user, "id", 1L);
                
        assertEquals(1L, user.getId());

        when(authenticationService.registerUser(any(User.class)))
            .thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"password123!\",\"email\":\"john.doe@gmail.com\"}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(authenticationService).registerUser(any(User.class));
    }

    @Test
    public void testRegisterShouldReturn400WhenCredentialsAreEmpty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}