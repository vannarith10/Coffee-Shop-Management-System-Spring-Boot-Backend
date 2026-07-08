package com.coffeeshop.api.mapper;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ImageStorageService imageStorageService;


    // (All Product Details)
    // MAP FROM PRODUCT TO PRODUCT ITEM RESPONSE
    public GetAllProductsResponse.ProductItem toProductItemResponseDto (Product product) {
        return GetAllProductsResponse.ProductItem
                .builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .description(product.getDescription())
                .imageUrl(imageStorageService.getImageUrl(product.getImageKey()))
                .categoryType(product.getCategory().getType())
                .categoryName(product.getCategory().getName())
                .stockStatus(product.getStockStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }


    // Product -> Product Item (Stock Status Specific, fewer details)
    // MAP FROM PRODUCT TO PRODUCT STOCK STATUS ITEM
    public ProductStockStatusResponse.ProductItem toProductStockStatusItemResponseDto (Product product) {
        return ProductStockStatusResponse.ProductItem
                .builder()
                .id(product.getId())
                .name(product.getName())
                .categoryName(product.getCategory().getName())
                .categoryType(product.getCategory().getType())
                .status(product.getStockStatus())
                .build();
    }


}
