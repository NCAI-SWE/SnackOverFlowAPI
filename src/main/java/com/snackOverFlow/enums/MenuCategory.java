package com.snackOverFlow.enums;

public enum MenuCategory {

    APPETIZER(1, "APPETIZER", "Starters and small plates"),
    MAIN(2, "MAIN", "Main courses"),
    DESSERT(3, "DESSERT", "Sweet items"),
    BEVERAGE(4, "BEVERAGE", "Drinks (hot/cold)");

    private final Integer id;
    private final String name;
    private final String description;

    MenuCategory(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public static MenuCategory fromId(Integer id) {
        if (id == null) throw new IllegalArgumentException("category_id cannot be null");
        for (MenuCategory c : values()) {
            if (c.id.equals(id)) return c;
        }
        throw new IllegalArgumentException("Unknown category_id: " + id);
    }

    public static MenuCategory fromName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("category_name cannot be null or blank");
        for (MenuCategory c : values()) {
            if (c.name.equalsIgnoreCase(name)) return c;
        }
        throw new IllegalArgumentException("Unknown category_name: " + name);
    }
}

