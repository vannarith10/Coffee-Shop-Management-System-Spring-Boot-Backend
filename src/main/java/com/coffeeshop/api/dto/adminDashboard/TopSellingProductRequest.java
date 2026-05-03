package com.coffeeshop.api.dto.adminDashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TopSellingProductRequest(

        @JsonProperty("range")
        TopSellingProductTimeRange range,

        @JsonProperty("page")
        Integer page,

        @JsonProperty("size")
        Integer size
) {
}
