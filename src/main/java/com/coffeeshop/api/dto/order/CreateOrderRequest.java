package com.coffeeshop.api.dto.order;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        String paymentMethod,   // "QR" or "CASH"
        String currency,        // "USD" or "KHR"
        String note, // ex: take away, VIP, Sugar 50% for all
        List<OrderItem> items
) {
    public record OrderItem(
            UUID productId,
            int quantity
    ) {}
}
