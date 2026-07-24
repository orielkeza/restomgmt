package com.restomgmt.site.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private String fullName;

    @Pattern(
        regexp = "ROLE_USER|ROLE_ADMIN|ROLE_STAFF",
        message = "Role must be one of: ROLE_USER, ROLE_ADMIN, ROLE_STAFF"
    )
    private String roleName = "ROLE_USER";
}