package com.coffeeshop.api.dto.adminDashboard.product;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
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
        String description,

        @JsonProperty("stock_status")
        ProductStock stockStatus
) {

        //
        public boolean isEmpty () {
                return (name == null || name.isBlank())
                        && (categoryName == null || categoryName.isBlank())
                        && (sellingPrice == null)
                        && (costPrice == null)
                        && (description == null || description.isBlank())
                        && (stockStatus == null);
        }
}
