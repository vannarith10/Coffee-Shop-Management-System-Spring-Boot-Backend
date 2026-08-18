package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.Pagination;
import com.coffeeshop.api.dto.product.GetProductMenu;
import com.coffeeshop.api.dto.product.MenuItemsResponse;
import com.coffeeshop.api.helper.PaginationHelper;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {


    private final AuthorizationGuard authorizationGuard;
    private final ImageStorageService imageStorageService;
    private final ProductRepository productRepository;



    // ===========================
    // GET Menu Items
    // ===========================
    @Override
    public GetProductMenu getMenu (int page, int size,
                                       CategoryType categoryType,
                                       String categoryName,
                                       String keyword) {
        authorizationGuard.requireAnyRoles(Role.CASHIER, Role.ADMIN);

        Pageable pageable = PaginationHelper.of(page, size, Sort.by("createdAt").ascending());
        Page<Product> products;

        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword) {
            // Search by product name
            products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            if (categoryType == null) {
                // All products
                products = productRepository.findAll(pageable);
            } else if (categoryName == null || categoryName.isBlank()) {
                // FOOD or DRINK
                products = productRepository.findByCategoryType(categoryType, pageable);
            } else {
                // FOOD + NOODLE
                // DRINK + COFFEE
                products = productRepository.findByCategoryTypeAndCategoryName(categoryType, categoryName, pageable);
            }
        }

        List<MenuItemsResponse> items = products.getContent().stream()
                .map(this::toMenuItem)
                .toList();

        var pagination = Pagination.builder()
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .totalPages(products.getTotalPages())
                .totalItems(products.getTotalElements())
                .build();

        return GetProductMenu.builder()
                .pagination(pagination)
                .items(items)
                .build();
    }

    // Mapper
    private MenuItemsResponse toMenuItem(Product p) {
        return MenuItemsResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .imageUrl(imageStorageService.getImageUrl(p.getImageKey()))
                .description(p.getDescription())
                .categoryType(p.getCategory().getType())
                .categoryName(p.getCategory().getName())
                .isAvailable(p.isAvailable())
                .stockStatus(p.getStockStatus())
                .build();
    }




    //------------------------------
    // GET MENU FOR CUSTOMERS
    //------------------------------
    @Override
    public Page<MenuItemsResponse> getMenuItemsForAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("price").ascending());


        return productRepository.findByAvailableTrue(pageRequest)
                .map(pro -> MenuItemsResponse.builder()
                        .id(pro.getId())
                        .name(pro.getName())
                        .price(pro.getPrice())
                        .imageUrl(imageStorageService.getImageUrl(pro.getImageKey()))
                        .description(pro.getDescription())
                        .categoryType(pro.getCategory().getType())
                        .categoryName(pro.getCategory().getName())
                        .isAvailable(pro.isAvailable())
                        .stockStatus(pro.getStockStatus())
                        .build());
    }


}







