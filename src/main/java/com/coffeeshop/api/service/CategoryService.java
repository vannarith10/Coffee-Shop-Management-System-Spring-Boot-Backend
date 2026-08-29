package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.dto.category.*;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    GetAllCategoriesResponse getAllCategories (int page, int size);

    void patchCategory (UUID categoryId, PatchCategoryRequest request);

    CategoryStatusResponse getCategoryStatus ();

    List<CategoryNameAndTypeResponse> getAllCategoryNames ();

}
