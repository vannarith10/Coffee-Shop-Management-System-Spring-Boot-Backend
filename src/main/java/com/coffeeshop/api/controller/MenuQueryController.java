package com.coffeeshop.api.controller;


import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.dto.product.GetProductMenu;
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
@RequestMapping("/api/v2/menu-query")
public class MenuQueryController {

    private final ProductCatalogService productCatalogService;



    // Menu
    // FOR CASHIER AND ADMIN
    @GetMapping
    public ResponseEntity<GetProductMenu> getMenu (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(value = "category_type", required = false) CategoryType categoryType,
            @RequestParam(value = "category_name", required = false) String categoryName,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(productCatalogService.getMenu(
                page,
                size,
                categoryType,
                categoryName,
                keyword));
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
