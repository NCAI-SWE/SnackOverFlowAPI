package com.snackOverFlow.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.snackOverFlow.enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessagePayload {

    private Long orderId;
    private Long customerId;
    private OrderStatus previousStatus;
    private OrderStatus currentStatus;
    private String notes;
    private OrderMessageMetadata metadata;
}
