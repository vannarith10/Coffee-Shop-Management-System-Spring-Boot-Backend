package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.dto.product.GetProductMenu;
import com.coffeeshop.api.dto.product.MenuItemsResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductCatalogService {

    GetProductMenu getMenu (int page, int size,
                                 CategoryType categoryType,
                                 String categoryName,
                                 String keyword);

    // No Login required
    Page<MenuItemsResponse> getMenuItemsForAllUsers (int page, int size);

}
