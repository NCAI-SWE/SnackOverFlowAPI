package com.snackOverFlow.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.snackOverFlow.enums.MessageType;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {

    private String messageId;
    private MessageType messageType;
    private Instant timestamp;
    private OrderMessagePayload payload;

    public static OrderMessage of(MessageType type, OrderMessagePayload payload) {
        return OrderMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(type)
                .timestamp(Instant.now())
                .payload(payload)
                .build();
    }
}

