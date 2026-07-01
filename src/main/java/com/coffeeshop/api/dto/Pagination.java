package com.coffeeshop.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Pagination(

        @JsonProperty("page")
        Integer page,

        @JsonProperty("size")
        Integer size,

        @JsonProperty("total_pages")
        Integer totalPages,

        @JsonProperty("total_items")
        Long totalItems
) {
}
