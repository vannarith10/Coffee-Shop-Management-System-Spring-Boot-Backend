package com.coffeeshop.api.dto.category;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotNull(message = "Category type is required")
        String type,

        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must be at most 100 characters")
        @Pattern(regexp = "^[\\p{Alnum} _-]+$", message = "Category name contains invalid characters")
        String name,

        @JsonProperty("is_active")
        boolean isActive
) {
}
