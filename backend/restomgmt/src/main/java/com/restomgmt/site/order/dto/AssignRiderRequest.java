package com.restomgmt.site.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRiderRequest {

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$",
             message = "Invalid phone number format")
    private String riderPhone;

    private String deliveryNote;
}