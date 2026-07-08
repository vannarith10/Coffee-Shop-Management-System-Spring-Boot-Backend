package com.coffeeshop.api.dto.adminDashboard.product;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.dto.Pagination;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record GetAllProductsResponse(

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("product_items")
        List<ProductItem> productItems
) {

    @Builder
    public record ProductItem (
            @JsonProperty("id")
            UUID id,

            @JsonProperty("name")
            String name,

            @JsonProperty("price")
            BigDecimal price,

            @JsonProperty("cost_price")
            BigDecimal costPrice,

            @JsonProperty("description")
            String description,

            @JsonProperty("image_url")
            String imageUrl,

            @JsonProperty("category_type")
            CategoryType categoryType,

            @JsonProperty("category_name")
            String categoryName,

            @JsonProperty("stock_status")
            ProductStock stockStatus,

            @JsonProperty("created_at")
            Instant createdAt,

            @JsonProperty("updated_at")
            Instant updatedAt
    ) {}
}
