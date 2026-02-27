package com.coffeeshop.api.dto.product;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record MenuItemsResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("name")
        String name,

        @JsonProperty("price")
        BigDecimal price,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("description")
        String description,

        @JsonProperty("category_type")
        CategoryType categoryType, // Food, Drink

        @JsonProperty("category_name")
        String categoryName, // Coffee, Tea, Bread, Snack

        @JsonProperty("in_stock")
        boolean inStock
) {
}
