package com.coffeeshop.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CreateProductResponse(
        String status,
        String message,
        ProductDate data
) {
    @Builder
    public record ProductDate(
            @JsonProperty("product_id")
            UUID productId,

            @JsonProperty("image_url")
            String imageUrl,

            @JsonProperty("created_at")
            Instant createdAt
    ){}
}
