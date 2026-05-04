package com.snackOverFlow.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.snackOverFlow.entity.Order;
import com.snackOverFlow.enums.OrderStatus;
import com.snackOverFlow.messaging.OrderEventPublisher;
import com.snackOverFlow.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Automatically cancels PENDING orders not confirmed within 10 minutes.
 */
@Component
@Slf4j
public class OrderAutoCancellationScheduler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Scheduled(fixedDelayString = "${scheduler.auto-cancel.fixed-delay-ms}")
    @Transactional
    public void cancelExpiredPendingOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        List<Order> expiredOrders = orderRepository
                .findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        if (expiredOrders.isEmpty()) return;

        log.info("Auto-cancelling {} expired pending orders", expiredOrders.size());

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancellationReason("Auto-cancelled: not confirmed within 10 minutes");
            order.setNotes(order.getCancellationReason());
            orderRepository.save(order);
            orderEventPublisher.publishOrderStatusChanged(order, OrderStatus.PENDING);
            log.info("Auto-cancelled order: id={}", order.getId());
        }
    }
}
