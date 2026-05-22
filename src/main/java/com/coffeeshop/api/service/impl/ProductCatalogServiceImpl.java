package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.product.MenuItemsResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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



    //-----------------
    // GET MENU
    //-----------------
    @Override
    public List<MenuItemsResponse> getMenuItems() {
        User user = authorizationGuard.requireAnyRoles(Role.CASHIER, Role.ADMIN);
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"));
        return products.stream().map(this::toMenuItem).toList();
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
                .inStock(p.isAvailable())
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
                        .inStock(pro.isAvailable())
                        .build());
    }


}







