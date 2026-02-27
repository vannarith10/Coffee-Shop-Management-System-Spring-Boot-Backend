package com.coffeeshop.api.dto.order;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderMessage(
        UUID orderId,
        String status, // PENDING, PREPARING, DONE
        Instant createdAt,
        List<Item> items
) {
    public record Item(
            String name,
            int quantity
    ){}
}
