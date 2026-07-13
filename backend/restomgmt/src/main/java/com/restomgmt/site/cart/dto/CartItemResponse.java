package com.restomgmt.site.cart.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {
    private Long menuItemId;
    private String itemName;
    private BigDecimal itemPrice;
    private int quantity;
    private BigDecimal subtotal;
}
