package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.dto.category.CategoryResponse;
import com.coffeeshop.api.dto.category.CreateCategoryRequest;
import com.coffeeshop.api.dto.category.GetAllCategoriesResponse;
import com.coffeeshop.api.dto.category.PatchCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    GetAllCategoriesResponse getAllCategoies (int page, int size);

    CategoryResponse patchCategory (UUID categoryId, PatchCategoryRequest request);

    CategoryResponse updateName(UUID id, String newName);

    void activate(UUID id);

    void deactivate(UUID id);

    List<CategoryResponse> getActiveCategories();

    Category getById(UUID id);

}
