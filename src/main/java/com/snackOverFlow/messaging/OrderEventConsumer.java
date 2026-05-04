package com.snackOverFlow.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.snackOverFlow.config.rabbitmq.OrdersRabbitMQConfig;
import com.snackOverFlow.dto.messaging.OrderMessage;
import com.snackOverFlow.entity.ProcessedMessage;
import com.snackOverFlow.repository.ProcessedMessageRepository;

@Component
@Slf4j
public class OrderEventConsumer {

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    /**
     * Consumes all order events from the notification queue.
     * Handles idempotency by checking the processed messages table.
     */
    @RabbitListener(queues = OrdersRabbitMQConfig.QUEUE_NOTIFICATION,
                    containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void handleOrderEvent(@Payload OrderMessage message) {
        String messageId = message.getMessageId();

        // ── Idempotency check ────────────────────────────────────────────────
        if (processedMessageRepository.existsByMessageId(messageId)) {
            log.warn("Duplicate message detected, skipping: messageId={}", messageId);
            return;
        }

        log.info("Processing order event: type={} orderId={} messageId={}",
                message.getMessageType().getName(),
                message.getPayload().getOrderId(),
                messageId);

        // ── Business logic per event type ────────────────────────────────────
        switch (message.getMessageType()) {
            case ORDER_PLACED ->
                    log.info("Order placed: orderId={} amount={}",
                            message.getPayload().getOrderId(),
                            message.getPayload().getMetadata().getAmount());

            case ORDER_CONFIRMED ->
                    log.info("Order confirmed: orderId={} preparationTime={} min",
                            message.getPayload().getOrderId(),
                            message.getPayload().getMetadata().getPreparationTime());

            case ORDER_STATUS_CHANGED ->
                    log.info("Order status changed: orderId={} {} → {}",
                            message.getPayload().getOrderId(),
                            message.getPayload().getPreviousStatus() != null
                                    ? message.getPayload().getPreviousStatus().getName() : "N/A",
                            message.getPayload().getCurrentStatus().getName());

            case ORDER_CANCELLED ->
                    log.info("Order cancelled: orderId={} reason={}",
                            message.getPayload().getOrderId(),
                            message.getPayload().getNotes());
        }

        // ── Mark message as processed ────────────────────────────────────────
        processedMessageRepository.save(ProcessedMessage.builder()
                .messageId(messageId)
                .messageType(message.getMessageType().getName())
                .build());
    }
}
