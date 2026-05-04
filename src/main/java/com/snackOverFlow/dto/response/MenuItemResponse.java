package com.snackOverFlow.dto.response;

import lombok.Data;
import com.snackOverFlow.enums.MenuCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuCategory category;
    private Integer preparationTimeMinutes;
    private Boolean isAvailable;
    private Integer caloriesPerServing;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
