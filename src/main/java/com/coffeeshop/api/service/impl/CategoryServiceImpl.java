package com.coffeeshop.api.service.impl;


import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.Pagination;
import com.coffeeshop.api.dto.category.*;
import com.coffeeshop.api.helper.PaginationHelper;
import com.coffeeshop.api.mapper.CategoryMapper;
import com.coffeeshop.api.repository.CategoryRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.CategoryService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {


    private final CategoryRepository categoryRepository;
    private final AuthorizationGuard authorizationGuard;
    private final CategoryMapper categoryMapper;
    private final WebSocketEventPublisher webSocketEventPublisher;


    // ===============================
    // Create Category
    // ===============================
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        authorizationGuard.requireAdmin();

        // Validation
        if (request.type() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category type is required"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category name is required"
            );
        }


        String normalizedName = request.name().trim().toUpperCase();
        String temp = request.type().trim().toUpperCase();
        CategoryType categoryType;

        try {
            categoryType = CategoryType.valueOf(temp);
        } catch (IllegalArgumentException ex) {
            String allowed = Arrays.stream(CategoryType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid category type: '" + temp + "'. Allowed values: " + allowed
            );
        }

        if (categoryRepository.existsByName(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        Category category = Category.builder()
                .type(categoryType)
                .name(normalizedName)
                .active(request.isActive())
                .build();

        Category saved = categoryRepository.save(category);

        // WebSocket | Send Category
        var response = CategoryResponse.builder()
                .categoryId(saved.getId())
                .categoryName(saved.getName())
                .categoryType(saved.getType())
                .isActive(saved.isActive())
                .build();
        webSocketEventPublisher.publishCategoryCreateToAdmins(response);

        // WebSocket | Send Summary Status
        var status = CategoryStatusResponse.builder()
                .totalCategories(categoryRepository.count())
                .totalDrinks(categoryRepository.countByType(CategoryType.DRINK))
                .totalFoods(categoryRepository.countByType(CategoryType.FOOD))
                .totalDisables(categoryRepository.countByActiveFalse())
                .build();
        webSocketEventPublisher.publishCategoryStatusSummaryToAdmins(status);

        return response;
    }




    // =============================
    // Get Category Status
    // =============================
    @Override
    public CategoryStatusResponse getCategoryStatus() {
        authorizationGuard.requireAdmin();

        return CategoryStatusResponse.builder()
                .totalCategories(categoryRepository.count())
                .totalDrinks(categoryRepository.countByType(CategoryType.DRINK))
                .totalFoods(categoryRepository.countByType(CategoryType.FOOD))
                .totalDisables(categoryRepository.countByActiveFalse())
                .build();
    }




    //=========================
    // Patch Category
    //=========================
    @Transactional
    @Override
    public CategoryResponse patchCategory(UUID categoryId, PatchCategoryRequest request) {
        // Name, Type, Status
        authorizationGuard.requireAdmin();
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
        );

        // Name
        if (request.newName() != null && !request.newName().isBlank()) {
            String name = request.newName().trim().toUpperCase();
            if (!name.equals(category.getName()) && categoryRepository.existsByName(name)){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exist");
            } else {
            category.setName(name);
            }
        }
        // Type
        if (request.newType() != null) {
            category.setType(request.newType());
        }
        // Status
        if (request.newStatus() != null) {
            category.setActive(request.newStatus());
        }

        Category saved = categoryRepository.save(category);


        // WebSocket | send new value
        var response = CategoryResponse.builder()
                .categoryId(saved.getId())
                .categoryName(saved.getName())
                .categoryType(saved.getType())
                .isActive(saved.isActive())
                .build();
        webSocketEventPublisher.publishCategoryUpdateToAdmins(response);

        // Send new status
        var res = CategoryStatusResponse.builder()
                .totalCategories(categoryRepository.count())
                .totalDrinks(categoryRepository.countByType(CategoryType.DRINK))
                .totalFoods(categoryRepository.countByType(CategoryType.FOOD))
                .totalDisables(categoryRepository.countByActiveFalse())
                .build();
        webSocketEventPublisher.publishCategoryStatusSummaryToAdmins(res);

        return response;
    }




    //=============================
    // Get all Categories
    //=============================
    @Override
    public GetAllCategoriesResponse getAllCategories(int page, int size) {
        authorizationGuard.requireAdmin();

        Pageable pageable = PaginationHelper.of(page, size);
        Page<Category> categories = categoryRepository.findAll(pageable);

        List<GetAllCategoriesResponse.Category> categoryList = categories
                .getContent()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();

        var pagination = Pagination.builder()
                .page(categories.getNumber() + 1)
                .size(categories.getSize())
                .itemCount(categories.getNumberOfElements())
                .totalPages(categories.getTotalPages())
                .totalItems(categories.getTotalElements())
                .build();

        return GetAllCategoriesResponse.builder()
                .pagination(pagination)
                .categories(categoryList)
                .build();
    }






    // ==============================
    // Get Category names & type only
    // ==============================
    @Override
    public List<CategoryNameAndTypeResponse> getAllCategoryNames() {
        authorizationGuard.requireAnyRoles(Role.ADMIN, Role.CASHIER);
        return categoryRepository.findAllNamesAndTypes();
    }



}
