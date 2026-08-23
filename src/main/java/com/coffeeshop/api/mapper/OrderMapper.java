package com.coffeeshop.api.mapper;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.BaristaOrderItem;
import com.coffeeshop.api.minio.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ImageStorageService imageStorageService;


    public BaristaOrderItem toBaristaOrderItem (Order order) {
        return BaristaOrderItem.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(
                        od -> BaristaOrderItem.Item.builder()
                                .itemId(od.getId())
                                .name(od.getProduct().getName())
                                .imageUrl(imageStorageService.getImageUrl(od.getProduct().getImageKey()))
                                .quantity(od.getQuantity())
                                .build()
                ).toList())
                .build();
    }

}