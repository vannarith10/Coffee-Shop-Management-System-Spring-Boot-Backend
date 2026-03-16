package com.coffeeshop.api.dto.product;

import java.util.UUID;

public record TopSellingProductRow(
        UUID productId,
        String productName,
        String imageKey,
        long unitsSold
) {
}
