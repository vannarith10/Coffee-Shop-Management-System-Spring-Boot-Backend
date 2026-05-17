package com.coffeeshop.api.dto.adminDashboard.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AddNewProductRequest(

        @JsonProperty("name")
        String name,

        @JsonProperty("selling_price")
        BigDecimal sellingPrice,

        @JsonProperty("cost_price")
        BigDecimal costPrice,

        @JsonProperty("category_name")
        String categoryName,

        @JsonProperty("stock_status")
        String stockStatus,

        @JsonProperty("description")
        String description
) {
}
