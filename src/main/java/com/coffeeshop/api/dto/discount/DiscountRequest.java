package com.coffeeshop.api.dto.discount;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DiscountRequest(
        @JsonProperty("product_id")
        UUID productId,

        @JsonProperty("rate")
        BigDecimal rate,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate
) {
}
