package com.restomgmt.site.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.restomgmt.site.user.controllers.AuthenticationController;
import com.restomgmt.site.user.dto.RegisterRequest;
import com.restomgmt.site.user.dto.ResetPasswordRequest;
import com.restomgmt.site.user.security.AuthenticationService;
import com.restomgmt.site.user.services.EmailService;
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

    @MockitoBean
    private EmailService emailService;

    // login
    @Test
    void loginShouldReturn200WhenCredentialsAreValid() throws Exception {
        when(authenticationService.authenticate("theJohnD", "Password123!"))
            .thenReturn("mocked-jwt-token");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"Password123!\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("mocked-jwt-token"));

        verify(authenticationService).authenticate("theJohnD", "Password123!");
    }

    @Test
    void loginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
        when(authenticationService.authenticate(any(), any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"wrong\"}"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void loginShouldReturn401WithMessageWhenEmailNotVerified() throws Exception {
        when(authenticationService.authenticate(any(), any()))
            .thenThrow(new DisabledException("Email not verified"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"Password123!\"}"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.content()
                .string("Email not verified. Please check your inbox."));
    }

    // register
    @Test
    void registerShouldReturn201WhenValid() throws Exception {
        doNothing().when(authenticationService).register(any(RegisterRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"Password123!\"," +
                    "\"email\":\"john.doe@gmail.com\",\"fullName\":\"John Doe\"}"))
            .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    void registerShouldReturn400WhenBodyIsEmpty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void registerShouldReturn400WhenPasswordDoesNotMeetConstraints() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"weakpassword\"," +
                    "\"email\":\"john.doe@gmail.com\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void registerShouldReturn400WhenUsernameAlreadyTaken() throws Exception {
        doThrow(new IllegalArgumentException("Username already taken"))
            .when(authenticationService).register(any(RegisterRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"theJohnD\",\"password\":\"Password123!\"," +
                    "\"email\":\"john.doe@gmail.com\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // verify email
    @Test
    void verifyEmailShouldReturn200WhenTokenIsValid() throws Exception {
        doNothing().when(authenticationService).verifyEmail("valid-token");

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/verify-email")
                .param("token", "valid-token"))
            .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService).verifyEmail("valid-token");
    }

    @Test
    void verifyEmailShouldReturn400WhenTokenIsInvalid() throws Exception {
        doThrow(new IllegalArgumentException("Invalid token"))
            .when(authenticationService).verifyEmail("bad-token");

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/verify-email")
                .param("token", "bad-token"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void verifyEmailShouldReturn400WhenTokenIsExpired() throws Exception {
        doThrow(new IllegalArgumentException("Token has expired"))
            .when(authenticationService).verifyEmail("expired-token");

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/verify-email")
                .param("token", "expired-token"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // forgot password
    @Test
    void forgotPasswordShouldAlwaysReturn200() throws Exception {
        doNothing().when(authenticationService).requestPasswordReset(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john.doe@gmail.com\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void forgotPasswordShouldReturn400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"notanemail\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // reset password
    @Test
    void resetPasswordShouldReturn200WhenValid() throws Exception {
        doNothing().when(authenticationService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"valid-token\",\"newPassword\":\"NewPassword123!\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void resetPasswordShouldReturn400WhenTokenIsExpired() throws Exception {
        doThrow(new IllegalArgumentException("Token has expired"))
            .when(authenticationService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"expired\",\"newPassword\":\"NewPassword123!\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void resetPasswordShouldReturn400WhenPasswordDoesNotMeetConstraints() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"valid-token\",\"newPassword\":\"weakpassword\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // resend verification
    @Test
    void resendVerificationShouldReturn200WhenValid() throws Exception {
        doNothing().when(authenticationService).resendVerification(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john.doe@gmail.com\"}"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void resendVerificationShouldReturn400WhenAlreadyVerified() throws Exception {
        doThrow(new IllegalStateException("Email already verified"))
            .when(authenticationService).resendVerification(any());

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john.doe@gmail.com\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}