package com.snackOverFlow.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.snackOverFlow.config.rabbitmq.OrdersRabbitMQConfig;
import com.snackOverFlow.dto.messaging.OrderMessage;
import com.snackOverFlow.dto.messaging.OrderMessageMetadata;
import com.snackOverFlow.dto.messaging.OrderMessagePayload;
import com.snackOverFlow.entity.Order;
import com.snackOverFlow.enums.MessageType;
import com.snackOverFlow.enums.OrderStatus;

@Component
@Slf4j
public class OrderEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderPlaced(Order order) {
        OrderMessage message = buildMessage(MessageType.ORDER_PLACED, order, null, OrderStatus.PENDING);
        send("order.placed", message);
    }

    public void publishOrderStatusChanged(Order order, OrderStatus previousStatus) {
        MessageType type = order.getStatus() == OrderStatus.CONFIRMED
                ? MessageType.ORDER_CONFIRMED
                : order.getStatus() == OrderStatus.CANCELLED
                ? MessageType.ORDER_CANCELLED
                : MessageType.ORDER_STATUS_CHANGED;

        OrderMessage message = buildMessage(type, order, previousStatus, order.getStatus());
        String routingKey = "order.status." + order.getStatus().getName().toLowerCase();
        send(routingKey, message);
    }

    private void send(String routingKey, OrderMessage message) {
        try {
            rabbitTemplate.convertAndSend(OrdersRabbitMQConfig.ORDERS_EXCHANGE, routingKey, message);
            log.info("Published [{}] for order {} routing='{}'",
                    message.getMessageType().getName(), message.getPayload().getOrderId(), routingKey);
        } catch (Exception ex) {
            log.error("Failed to publish message for order {}: {}",
                    message.getPayload().getOrderId(), ex.getMessage(), ex);
        }
    }

    private OrderMessage buildMessage(MessageType type, Order order,
                                      OrderStatus previousStatus, OrderStatus currentStatus) {
        OrderMessagePayload payload = OrderMessagePayload.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .previousStatus(previousStatus)
                .currentStatus(currentStatus)
                .notes(order.getNotes())
                .metadata(OrderMessageMetadata.builder()
                        .preparationTime(order.getPreparationTimeMinutes())
                        .amount(order.getTotalAmount())
                        .itemCount(order.getItems().size())
                        .build())
                .build();
        return OrderMessage.of(type, payload);
    }
}
