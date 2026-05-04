package com.snackOverFlow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.snackOverFlow.enums.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;
}

