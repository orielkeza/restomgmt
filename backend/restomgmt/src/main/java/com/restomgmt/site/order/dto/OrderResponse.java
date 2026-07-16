package com.restomgmt.site.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.restomgmt.site.BaseEntity;
import com.restomgmt.site.order.models.OrderStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse extends BaseEntity{
    
    private Long orderId;
    private String username;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private BigDecimal total;
    private List<String> warnings;
    private String riderPhone;
    private String deliveryNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
