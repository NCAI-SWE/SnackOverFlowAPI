package com.snackOverFlow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.snackOverFlow.dto.response.OrderItemResponse;
import com.snackOverFlow.dto.response.OrderResponse;
import com.snackOverFlow.entity.Order;
import com.snackOverFlow.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    OrderResponse toResponse(Order order);

    @Mapping(target = "menuItemId", source = "menuItem.id")
    @Mapping(target = "menuItemName", source = "menuItem.name")
    OrderItemResponse toItemResponse(OrderItem item);
}

