package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.product.CreateProductResponse;
import com.coffeeshop.api.dto.product.MenuItemsResponse;
import com.coffeeshop.api.dto.product.UpdateProductPatchRequest;
import com.coffeeshop.api.dto.product.UpdateProductPriceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    CreateProductResponse createProduct (
            String name,
            BigDecimal price,
            BigDecimal costPrice,
            String categoryName,
            boolean status,
            MultipartFile image
    );

    List<MenuItemsResponse> getMenuItems ();

    void updateStockStatus (UUID productId, boolean available);

    UpdateProductPriceResponse updateProductPrice (UUID productId, BigDecimal productPrice);

    MenuItemsResponse patchProduct (UUID productId, UpdateProductPatchRequest patch);

}
