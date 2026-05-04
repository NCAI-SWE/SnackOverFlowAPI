package com.snackOverFlow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.snackOverFlow.enums.MenuCategory;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private MenuCategory category;

    @NotNull(message = "Preparation time is required")
    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    private Integer preparationTimeMinutes;

    private Boolean isAvailable = true;

    @Min(value = 0)
    private Integer caloriesPerServing;
}

