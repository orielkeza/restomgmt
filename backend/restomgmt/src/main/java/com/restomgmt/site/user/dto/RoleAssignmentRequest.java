package com.restomgmt.site.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleAssignmentRequest {
    @NotBlank
    private String roleName;
}
