package com.coffeeshop.api.mapper;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.OrderMessageToBarista;
import com.coffeeshop.api.minio.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ImageStorageService imageStorageService;


    // OLD
    public static OrderMessageToBarista toOrderMessage (Order order , ImageStorageService imageStorageService) {
        return OrderMessageToBarista.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(
                        orderItem -> new OrderMessageToBarista.Item(
                                orderItem.getId(),
                                orderItem.getProduct().getName(),
                                orderItem.getProduct().getImageKey() == null ? null : imageStorageService.getPresignedGetUrl(orderItem.getProduct().getImageKey()).toString(),
                                orderItem.getQuantity()
                        )
                ).collect(Collectors.toList()))
                .build();
    }



    // NEW VERSION - Don't need imageStorageService parameter
    public OrderMessageToBarista toOrderMessageResponseDto (Order order) {
        return OrderMessageToBarista.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(
                        od -> OrderMessageToBarista.Item.builder()
                                .itemId(od.getId())
                                .name(od.getProduct().getName())
                                .imageUrl(imageStorageService.getImageUrl(od.getProduct().getImageKey()))
                                .quantity(od.getQuantity())
                                .build()
                ).toList())
                .build();
    }

}