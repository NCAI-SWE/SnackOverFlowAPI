package com.snackOverFlow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.snackOverFlow.enums.MenuCategory;

import java.math.BigDecimal;

@Data
public class UpdateMenuItemRequest {

    @Size(max = 200)
    private String name;

    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private MenuCategory category;

    @Min(1)
    private Integer preparationTimeMinutes;

    private Boolean isAvailable;

    @Min(0)
    private Integer caloriesPerServing;
}

