package com.coffeeshop.api.dto.adminDashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record TopSellingProductProjection(

        UUID productId,

        String productName,

        String imageKey,

        Long unitsSold // JPQL -> Sum always returns Long
) {
}
