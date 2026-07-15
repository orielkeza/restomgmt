package com.restomgmt.site.user.dto;

import com.restomgmt.site.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
   
    @NotBlank
    private String token;

    @NotBlank
    @ValidPassword
    private String newPassword;
    
}
