package com.coffeeshop.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductEvent(
        String event,

        @JsonProperty("product_id")
        UUID productId,

        Object payload
) {
}
