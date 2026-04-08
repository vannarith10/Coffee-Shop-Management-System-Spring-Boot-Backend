package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.product.*;
import org.springframework.data.domain.Page;
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

    MenuItemsResponse updateProductImage (UUID productId, MultipartFile image);

    TopSellingProductsResponse topSellingProducts ();

    // It acts like "getMenuItems" but just no ROLE validation
    // - I don't want to touch "getMenuItems" it works well
    // - This time I want to use it with Pagination
    Page<MenuItemsResponse> getMenuItemsForAllUsers(int page, int size);

}
