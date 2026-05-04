package com.snackOverFlow.enums;

public enum MessageType {

    ORDER_PLACED(1, "ORDER_PLACED"),
    ORDER_CONFIRMED(2, "ORDER_CONFIRMED"),
    ORDER_STATUS_CHANGED(3, "ORDER_STATUS_CHANGED"),
    ORDER_CANCELLED(4, "ORDER_CANCELLED");

    private final Integer id;
    private final String name;

    MessageType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }

    public static MessageType fromId(Integer id) {
        if (id == null) throw new IllegalArgumentException("message_type_id cannot be null");
        for (MessageType t : values()) {
            if (t.id.equals(id)) return t;
        }
        throw new IllegalArgumentException("Unknown message_type_id: " + id);
    }

    public static MessageType fromName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("message_type_name cannot be null or blank");
        for (MessageType t : values()) {
            if (t.name.equalsIgnoreCase(name)) return t;
        }
        throw new IllegalArgumentException("Unknown message_type_name: " + name);
    }
}

