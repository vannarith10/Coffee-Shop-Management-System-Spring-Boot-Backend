package com.coffeeshop.api.controller;


import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.dto.adminDashboard.product.AddNewProductRequest;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.coffeeshop.api.dto.adminDashboard.product.UpdateProductRequest;
import com.coffeeshop.api.service.ProductAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/product")
@PreAuthorize("hasRole('ADMIN')")
public class ProductAdminController {

    private final ProductAdminService productAdminService;


    // GET ALL PRODUCTS
    @GetMapping("/get-all-products")
    public ResponseEntity<GetAllProductsResponse> getProducts (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productAdminService.getAllProducts(page, size));
    }



    // GET STOCK STATUSES
    @GetMapping("/get-statuses")
    public ResponseEntity<ProductStockStatusResponse> stockStatus (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productAdminService.GetAllProductStockStatus(page, size));
    }



    // UPDATE STOCK
    @PostMapping("/update/{id}/stock-status")
    public void updateStockStatus (
            @PathVariable UUID id,
            @RequestParam ProductStock status
            ) {
        productAdminService.updateProductStockStatus(id, status);
    }



    // ADD NEW PRODUCT
    @PostMapping(value = "/add-new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GetAllProductsResponse.ProductItem> addProduct (
            @RequestParam("name") @NotBlank String name,
            @RequestParam("price")BigDecimal price,
            @RequestParam("cost") BigDecimal cost,
            @RequestParam("category_name") String categoryName,
            @RequestParam("stock_status") String stockStatus,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false)MultipartFile image
            ) {
        var request = AddNewProductRequest.builder()
                .name(name)
                .sellingPrice(price)
                .costPrice(cost)
                .categoryName(categoryName)
                .stockStatus(stockStatus)
                .description(description)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(productAdminService.addNewProduct(request, image));
    }



    // PATCH PRODUCT
    @PatchMapping(value = "/{id}/patch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GetAllProductsResponse.ProductItem> patchProduct (
            @PathVariable UUID id,
            @RequestPart("data") @Valid UpdateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
            ) {
        return ResponseEntity.ok(productAdminService.updateProductPartially(id, request, image));
    }

}












