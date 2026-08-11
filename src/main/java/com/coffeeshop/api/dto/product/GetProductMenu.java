package com.coffeeshop.api.dto.product;

import com.coffeeshop.api.dto.Pagination;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record GetProductMenu(

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("items")
        List<MenuItemsResponse> items
) {
}
