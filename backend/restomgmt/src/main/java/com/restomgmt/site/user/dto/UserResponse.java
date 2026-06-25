package com.restomgmt.site.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean enabled;
}
