package com.coffeeshop.api.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderMessageToBarista(
        @JsonProperty("order_id")
        UUID orderId,

        @JsonProperty("order_number")
        String orderNumber,

        String status, // PENDING, PREPARING, DONE

        @JsonProperty("create_at")
        Instant createdAt,

        List<Item> items
) {
    public record Item(
            String name,

            @JsonProperty("image_url")
            String imageUrl,

            int quantity
    ){}
}
