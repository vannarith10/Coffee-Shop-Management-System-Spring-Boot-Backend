package com.coffeeshop.api.mapper;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.OrderMessageToBarista;
import com.coffeeshop.api.minio.ImageStorageService;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;


@RequiredArgsConstructor
public class OrderMapper {


    public static OrderMessageToBarista toOrderMessage (Order order , ImageStorageService imageStorageService) {
        return OrderMessageToBarista.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(
                        orderItem -> new OrderMessageToBarista.Item(
                                orderItem.getProduct().getName(),
                                imageStorageService.getPresignedGetUrl(orderItem.getProduct().getImageKey()).toString(),
                                orderItem.getQuantity()
                        )
                ).collect(Collectors.toList()))
                .build();
    }

}