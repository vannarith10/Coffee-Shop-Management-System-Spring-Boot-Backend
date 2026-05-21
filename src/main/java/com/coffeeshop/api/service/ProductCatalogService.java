package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.product.MenuItemsResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductCatalogService {

    List<MenuItemsResponse> getMenuItems ();

    // No Login required
    Page<MenuItemsResponse> getMenuItemsForAllUsers (int page, int size);

}
