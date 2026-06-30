package com.coffeeshop.api.dto.category;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record GetAllCategoriesResponse(

        List<Category> categories

) {

    @Builder
    public record Category (
            @JsonProperty("category_name")
            String categoryName,

            @JsonProperty("category_response")
            CategoryType categoryType,

            @JsonProperty("is_active")
            boolean isActive
    ) {}
}
