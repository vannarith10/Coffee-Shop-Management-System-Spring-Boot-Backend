package com.coffeeshop.api.dto.category;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PatchCategoryRequest (

        @JsonProperty("new_name")
        String newName,

        @JsonProperty("new_type")
        CategoryType newType,

        @JsonProperty("new_status")
        Boolean newStatus

) {
}
