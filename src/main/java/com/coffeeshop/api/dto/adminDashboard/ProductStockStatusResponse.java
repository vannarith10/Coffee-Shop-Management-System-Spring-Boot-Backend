package com.coffeeshop.api.dto.adminDashboard;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProductStockStatusResponse(
        @JsonProperty("message")
        String message,

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("products")
        List<ProductItem> products
) {
    @Builder
    public record ProductItem (
            @JsonProperty("id")
            UUID id,

            @JsonProperty("name")
            String name,

            @JsonProperty("category_name")
            String categoryName,

            @JsonProperty("category_type")
            CategoryType categoryType,

            @JsonProperty("status")
            ProductStock status
    ){}

    @Builder
    public record Pagination (
            @JsonProperty("page")
            Integer page,

            @JsonProperty("size")
            Integer size,

            @JsonProperty("total_pages")
            Integer totalPages,

            @JsonProperty("total_items")
            Long totalItems
    ) {}
}
