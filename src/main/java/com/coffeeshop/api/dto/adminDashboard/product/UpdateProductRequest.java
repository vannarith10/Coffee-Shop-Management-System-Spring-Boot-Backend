package com.coffeeshop.api.dto.adminDashboard.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(

        @JsonProperty("name")
        String name,

        @JsonProperty("category_name")
        String categoryName,

        @JsonProperty("selling_price")
        BigDecimal sellingPrice,

        @JsonProperty("cost_price")
        BigDecimal costPrice,

        @JsonProperty("description")
        String description
) {
        public boolean isEmpty () {
                return name == null
                        && categoryName == null
                        && sellingPrice == null
                        && costPrice == null
                        && description == null;
        }
}
