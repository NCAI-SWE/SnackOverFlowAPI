package com.snackOverFlow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.snackOverFlow.dto.request.CancelOrderRequest;
import com.snackOverFlow.dto.request.PlaceOrderRequest;
import com.snackOverFlow.dto.request.UpdateOrderStatusRequest;
import com.snackOverFlow.dto.response.OrderResponse;
import com.snackOverFlow.entity.Customer;
import com.snackOverFlow.entity.MenuItem;
import com.snackOverFlow.entity.Order;
import com.snackOverFlow.entity.OrderItem;
import com.snackOverFlow.enums.OrderStatus;
import com.snackOverFlow.exception.BusinessException;
import com.snackOverFlow.exception.ResourceNotFoundException;
import com.snackOverFlow.mapper.OrderMapper;
import com.snackOverFlow.messaging.OrderEventPublisher;
import com.snackOverFlow.repository.CustomerRepository;
import com.snackOverFlow.repository.MenuItemRepository;
import com.snackOverFlow.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderService {

    private static final BigDecimal MINIMUM_ORDER_VALUE = BigDecimal.valueOf(15);
    private static final int MAX_ACTIVE_ORDERS          = 20;
    private static final int PREPARATION_BUFFER_MINUTES = 5;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // ── Validate customer ───────────────────────────────────────────────
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        // ── Concurrent order limit ──────────────────────────────────────────
        long activeOrders = orderRepository.countByStatusIn(List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING));
        if (activeOrders >= MAX_ACTIVE_ORDERS) {
            throw new BusinessException("Maximum concurrent order limit (" + MAX_ACTIVE_ORDERS + ") reached. Try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        // ── Build order items ───────────────────────────────────────────────
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int maxPrepTime = 0;

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findByIdAndIsDeletedFalse(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemReq.getMenuItemId()));

            if (!menuItem.getIsAvailable()) {
                throw new BusinessException("Menu item '" + menuItem.getName() + "' is currently unavailable");
            }

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);
            maxPrepTime = Math.max(maxPrepTime, menuItem.getPreparationTimeMinutes());

            orderItems.add(OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .build());
        }

        // ── Minimum order value ─────────────────────────────────────────────
        if (total.compareTo(MINIMUM_ORDER_VALUE) < 0) {
            throw new BusinessException("Order total (" + total + " SAR) is below the minimum of " + MINIMUM_ORDER_VALUE + " SAR");
        }

        // ── Save order ──────────────────────────────────────────────────────
        Order order = Order.builder()
                .customer(customer)
                .totalAmount(total)
                .preparationTimeMinutes(maxPrepTime + PREPARATION_BUFFER_MINUTES)
                .notes(request.getNotes())
                .status(OrderStatus.PENDING)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);
        log.info("Order placed: id={} customer={} total={}", saved.getId(), customer.getId(), total);

        // ── Publish event ───────────────────────────────────────────────────
        orderEventPublisher.publishOrderPlaced(saved);

        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return orderMapper.toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return orders.map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        return orderRepository.findByCustomerId(customerId, pageable).map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = findOrder(id);
        OrderStatus previous = order.getStatus();
        OrderStatus next = request.getStatus();

        if (!previous.canTransitionTo(next)) {
            throw new BusinessException("Cannot transition order from " + previous.getName() + " to " + next.getName());
        }

        order.setStatus(next);
        Order saved = orderRepository.save(order);
        log.info("Order status updated: id={} {} → {}", id, previous.getName(), next.getName());

        orderEventPublisher.publishOrderStatusChanged(saved, previous);

        return orderMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, CancelOrderRequest request) {
        Order order = findOrder(id);

        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BusinessException("Cannot cancel order in status: " + order.getStatus().getName());
        }

        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(request.getReason());
        order.setNotes(request.getReason());

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={} reason={}", id, request.getReason());

        orderEventPublisher.publishOrderStatusChanged(saved, previous);

        return orderMapper.toResponse(saved);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
