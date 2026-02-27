package com.coffeeshop.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductPriceRequest(

        @NotNull(message = "Product ID must not be null")
        @JsonProperty("product_id")
        UUID productId,

        @NotNull(message = "New price must not be null")
        @Positive(message = "New price must be greater than 0")
        @JsonProperty("new_price")
        BigDecimal newPrice
) {
}
