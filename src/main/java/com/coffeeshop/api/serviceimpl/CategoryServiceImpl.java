package com.coffeeshop.api.serviceimpl;


import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.category.CategoryResponse;
import com.coffeeshop.api.dto.category.CreateCategoryRequest;
import com.coffeeshop.api.repository.CategoryRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.CategoryService;
import com.coffeeshop.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {


    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;



    // =============== Create Category =============== //
    // ===== Admin Only ===== //
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        // Role check
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can create category"
            );
        }

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

        if (categoryRepository.existsByTypeAndName(categoryType, normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category already exists"
            );
        }

        Category category = Category.builder()
                .type(categoryType)
                .name(normalizedName)
                .active(request.isActive())
                .build();

        categoryRepository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getType(),
                category.getName(),
                category.isActive()
        );
    }





    @Override
    public CategoryResponse updateName(UUID id, String newName) {
        return null;
    }





    @Override
    public void activate(UUID id) {

    }





    @Override
    public void deactivate(UUID id) {

    }





    @Override
    public List<CategoryResponse> getActiveCategories() {
        return List.of();
    }





    @Override
    public Category getById(UUID id) {
        return null;
    }




}
