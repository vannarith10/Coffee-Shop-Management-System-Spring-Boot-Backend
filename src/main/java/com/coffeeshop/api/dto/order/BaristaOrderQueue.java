package com.coffeeshop.api.dto.order;

import com.coffeeshop.api.dto.Pagination;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record BaristaOrderQueue(

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("barista_order_items")
        List<BaristaOrderItem> baristaOrderItems

) {
}
