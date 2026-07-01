package com.coffeeshop.api.dto.category;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.dto.Pagination;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record GetAllCategoriesResponse(

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("categories")
        List<Category> categories

) {

    @Builder
    public record Category (

            @JsonProperty("category_id")
            UUID categoryId,

            @JsonProperty("category_name")
            String categoryName,

            @JsonProperty("category_type")
            CategoryType categoryType,

            @JsonProperty("is_active")
            boolean isActive
    ) {}

}
