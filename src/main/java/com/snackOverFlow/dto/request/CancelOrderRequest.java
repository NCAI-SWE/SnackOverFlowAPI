package com.snackOverFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelOrderRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}

