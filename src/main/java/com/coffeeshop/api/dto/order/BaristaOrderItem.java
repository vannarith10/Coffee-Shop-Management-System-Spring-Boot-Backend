package com.coffeeshop.api.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record BaristaOrderItem(

        @JsonProperty("order_id")
        UUID orderId,

        @JsonProperty("order_number")
        String orderNumber,

        @JsonProperty("status")
        String status, // PENDING, PREPARING, DONE

        @JsonProperty("note")
        String note,

        @JsonProperty("create_at")
        Instant createdAt,

        @JsonProperty("items")
        List<Item> items
) {


    @Builder
    public record Item(
            @JsonProperty("item_id")
            UUID itemId,

            @JsonProperty("name")
            String name,

            @JsonProperty("image_url")
            String imageUrl,

            @JsonProperty("quantity")
            int quantity
    ){}
}
