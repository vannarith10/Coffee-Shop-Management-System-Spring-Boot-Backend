package com.coffeeshop.api.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CategoryStatusResponse(

        @JsonProperty("total_categories")
        long totalCategories,

        @JsonProperty("total_drinks")
        long totalDrinks,

        @JsonProperty("total_foods")
        long totalFoods,

        @JsonProperty("total_disables")
        long totalDisables
) {
}
