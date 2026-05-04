package com.snackOverFlow.enums;

public enum OrderStatus {

    PENDING(1, "PENDING"),
    CONFIRMED(2, "CONFIRMED"),
    PREPARING(3, "PREPARING"),
    READY(4, "READY"),
    CANCELLED(5, "CANCELLED");

    private final Integer id;
    private final String name;

    OrderStatus(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }

    public static OrderStatus fromId(Integer id) {
        if (id == null) throw new IllegalArgumentException("status_id cannot be null");
        for (OrderStatus s : values()) {
            if (s.id.equals(id)) return s;
        }
        throw new IllegalArgumentException("Unknown status_id: " + id);
    }

    public static OrderStatus fromName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("status_name cannot be null or blank");
        for (OrderStatus s : values()) {
            if (s.name.equalsIgnoreCase(name)) return s;
        }
        throw new IllegalArgumentException("Unknown status_name: " + name);
    }

    /**
     * Validates allowed transitions.
     * PENDING → CONFIRMED → PREPARING → READY
     * PENDING/CONFIRMED → CANCELLED
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == READY;
            case READY, CANCELLED -> false;
        };
    }
}

