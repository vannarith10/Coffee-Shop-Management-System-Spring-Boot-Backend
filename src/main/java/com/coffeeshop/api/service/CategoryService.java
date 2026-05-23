package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.dto.category.CategoryResponse;
import com.coffeeshop.api.dto.category.CreateCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateName(UUID id, String newName);

    void activate(UUID id);

    void deactivate(UUID id);

    List<CategoryResponse> getActiveCategories();

    Category getById(UUID id);

}
