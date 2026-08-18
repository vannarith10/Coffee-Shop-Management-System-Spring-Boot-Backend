package com.coffeeshop.api.dto.product;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record MenuItemsResponse(

// I use @NonNull to force every field to be required, but it will check at Runtime

        @NonNull
        @JsonProperty("id")
        UUID id,

        @NonNull
        @JsonProperty("name")
        String name,

        @NonNull
        @JsonProperty("price")
        BigDecimal price,

        @NonNull
        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("description")
        String description,

        @NonNull
        @JsonProperty("category_type")
        CategoryType categoryType, // Food, Drink

        @NonNull
        @JsonProperty("category_name")
        String categoryName, // Coffee, Tea, Bread, Snack

        @JsonProperty("is_available")
        boolean isAvailable,

        @NonNull
        @JsonProperty("stock_status")
        ProductStock stockStatus
) {
}
