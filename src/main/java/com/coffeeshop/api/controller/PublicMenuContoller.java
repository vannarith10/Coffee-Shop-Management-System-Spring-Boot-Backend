package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.product.MenuItemsResponse;
import com.coffeeshop.api.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/product")
public class PublicMenuContoller {

    private final ProductCatalogService productCatalogService;



    // FOR CASHIER AND ADMIN
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemsResponse>> getMenu () {
        return ResponseEntity.ok(productCatalogService.getMenuItems());
    }



    // FOR ALL CUSTOMERS
    @GetMapping("/user-menu")
    public Page<MenuItemsResponse> getMenuItems (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productCatalogService.getMenuItemsForAllUsers(page, size);
    }


}
