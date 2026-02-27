package com.coffeeshop.api.mapper;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.OrderMessage;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderMessage toOrderMessage (Order order) {
        return OrderMessage.builder()
                .orderId(order.getId())
                .status(order.getStatus().toString())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(
                        orderItem -> new OrderMessage.Item(
                                orderItem.getProduct().getName(),
                                orderItem.getQuantity()
                        )
                ).collect(Collectors.toList()))
                .build();
    }

}