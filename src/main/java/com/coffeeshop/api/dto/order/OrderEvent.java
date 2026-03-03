package com.coffeeshop.api.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record OrderEvent(
        String event,

        @JsonProperty("order_id")
        UUID orderId,

        Object payload
) {
}
