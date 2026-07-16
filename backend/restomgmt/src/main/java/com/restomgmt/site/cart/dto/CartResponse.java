package com.restomgmt.site.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.restomgmt.site.BaseEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartResponse extends BaseEntity {
    private Long cartId;
    private String username;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;    
}
