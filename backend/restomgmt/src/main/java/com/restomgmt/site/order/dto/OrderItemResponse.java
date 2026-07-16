package com.restomgmt.site.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {
    private Long menuItemId;
    private String itemName;
    private BigDecimal priceAtOrder;
    private int quantity;
    private BigDecimal subtotal;
}
