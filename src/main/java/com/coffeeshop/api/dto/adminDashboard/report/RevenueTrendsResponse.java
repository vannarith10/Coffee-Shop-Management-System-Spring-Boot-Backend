package com.coffeeshop.api.dto.adminDashboard.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RevenueTrendsResponse(

        @JsonProperty("day")
        String day,

        @JsonProperty("revenue")
        BigDecimal revenue

) {
}
