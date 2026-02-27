package com.coffeeshop.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record UpdateProductPriceResponse(

        @JsonProperty("product_id")
        UUID productId,

        @JsonProperty("new_price")
        BigDecimal newPrice
) {
}
