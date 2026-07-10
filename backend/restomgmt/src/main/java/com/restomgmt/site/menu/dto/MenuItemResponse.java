package com.restomgmt.site.menu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal cost;
    private boolean available;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
