package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.coffeeshop.api.dto.adminDashboard.product.AddNewProductRequest;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.UpdateProductRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProductAdminService {

    GetAllProductsResponse getAllProducts (int page, int size);

    ProductStockStatusResponse productStockStatus (int page, int size);


    GetAllProductsResponse.ProductItem addNewProduct (AddNewProductRequest request, MultipartFile image);

    GetAllProductsResponse.ProductItem updateProductPartially (UUID id, UpdateProductRequest request, MultipartFile image);

    void updateProductStockStatus (UUID id, ProductStock newStockStatus);

}
