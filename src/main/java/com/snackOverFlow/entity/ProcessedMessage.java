package com.snackOverFlow.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks processed RabbitMQ message IDs for idempotency.
 */
@Entity
@Table(name = "processed_messages", schema = "snackoverflow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedMessage {

    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}

