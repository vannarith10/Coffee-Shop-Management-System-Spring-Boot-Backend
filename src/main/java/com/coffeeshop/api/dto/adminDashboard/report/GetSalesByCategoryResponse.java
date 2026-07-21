package com.coffeeshop.api.dto.adminDashboard.report;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record GetSalesByCategoryResponse(

        @JsonProperty("category_id")
        UUID categoryId,

        @JsonProperty("category_name")
        String categoryName,

        @JsonProperty("category_type")
        CategoryType categoryType,

        @JsonProperty("revenue")
        BigDecimal revenue

) {

}
