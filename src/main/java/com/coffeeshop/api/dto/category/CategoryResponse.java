package com.coffeeshop.api.dto.category;

import com.coffeeshop.api.domain.enums.CategoryType;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        CategoryType type,
        String name,
        boolean active
) {
}
