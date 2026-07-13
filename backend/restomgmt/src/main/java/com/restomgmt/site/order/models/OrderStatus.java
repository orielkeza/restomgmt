package com.restomgmt.site.order.models;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    DELIVERED,
    CANCELLED;

    public OrderStatus next() {
        return switch (this) {
            case PENDING -> CONFIRMED;
            case CONFIRMED -> PREPARING;
            case PREPARING -> READY;
            case READY -> DELIVERED;
            default -> throw new IllegalStateException(
                "Cannot advance status from: " + this);
        };
    }

    public boolean canAdvance() {
        return this != DELIVERED && this != CANCELLED;
    }
}
