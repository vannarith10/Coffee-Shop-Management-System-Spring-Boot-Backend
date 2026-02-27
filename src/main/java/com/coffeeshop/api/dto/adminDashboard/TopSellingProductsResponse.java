package com.coffeeshop.api.dto.adminDashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TopSellingProductsResponse(

        @JsonProperty("unit_target")
        int unitTarget,

        List<ProductItem> products
) {
    public record ProductItem (
            UUID id,

            String name,

            @JsonProperty("units_sold")
            int unitsSold
    ) {}
}
