package com.coffeeshop.api.controller;

import com.coffeeshop.api.dto.product.*;
import com.coffeeshop.api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    //============================================================
    // Add Product
    //============================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateProductResponse> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "costPrice", required = false) BigDecimal costPrice,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "status", required = false) Boolean status,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        boolean available = status == null || status;
        var resp = productService.createProduct(name, price, costPrice, categoryName, available, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    //============================================================
    // Update Product Partially
    //============================================================
    @PatchMapping(path = "/{id}", consumes = {"application/merge-patch+json", "application/json"})
    public ResponseEntity<MenuItemsResponse> patch (@PathVariable UUID id, @RequestBody UpdateProductPatchRequest patch) {
        return ResponseEntity.ok(productService.patchProduct(id, patch));
    }


    //============================================================
    // Get product menu
    //============================================================
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemsResponse>> getMenuItems () {
        List<MenuItemsResponse> menu = productService.getMenuItems();
        return ResponseEntity.ok(menu);
    }


    @PutMapping("/{productId}/image")
    public ResponseEntity<MenuItemsResponse> updateImage (@PathVariable UUID productId,
                                                          @RequestParam("image") MultipartFile image) {

        return ResponseEntity.ok(productService.updateProductImage(productId, image));
    }


    //============================================================
    // Update product status
    //============================================================
    // TODO Test
    @PostMapping("/update-status")
    public void updateStatus (@RequestBody UpdateStatusRequest request) {
        productService.updateStockStatus(request.productId(), request.available);
    }

    public record UpdateStatusRequest (
            UUID productId,
            boolean available
    ){}


    //============================================================
    // Update product price
    //============================================================
    // TODO Now we have update patch
    @PostMapping("/update-price")
    public ResponseEntity<UpdateProductPriceResponse> updatePrice (
            @Valid @RequestBody UpdateProductPriceRequest request
            ) {
        UpdateProductPriceResponse response =
                productService.updateProductPrice(request.productId(), request.newPrice());

        return ResponseEntity.ok(response);
    }


}




















