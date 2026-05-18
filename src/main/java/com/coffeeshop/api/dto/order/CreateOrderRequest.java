package com.coffeeshop.api.dto.order;

import com.coffeeshop.api.domain.enums.CurrencyType;
import com.coffeeshop.api.domain.enums.PaymentMethod;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        PaymentMethod paymentMethod,   // "QR" or "CASH"
        CurrencyType currency,        // "USD" or "KHR"
        String note, // ex: take away, VIP, Sugar 50% for all
        List<OrderItem> items
) {
    public record OrderItem(
            UUID productId,
            int quantity
    ) {}
}
