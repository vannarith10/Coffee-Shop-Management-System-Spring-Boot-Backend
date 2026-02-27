package com.coffeeshop.api.dto.product;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;


public record UpdateProductPatchRequest(

        String name,

        BigDecimal price,

        String description,

        @JsonProperty("in_stock")
        Boolean inStock,

        @JsonProperty("category_name")
        String categoryName
) {
}
