package com.restomgmt.site.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitiatePaymentRequest {

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$",
             message = "Invalid phone number format")
    private String payerPhone;
}