package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.category.CategoryResponse;
import com.coffeeshop.api.dto.category.CreateCategoryRequest;
import com.coffeeshop.api.dto.category.GetAllCategoriesResponse;
import com.coffeeshop.api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/category")
public class CategoryController {

    private final CategoryService categoryService;


    // CREATE CATEGORY
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create (
            @Valid @RequestBody CreateCategoryRequest request
            ) {
        return ResponseEntity.status(201).body(categoryService.createCategory(request));
    }


    //
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetAllCategoriesResponse> getAllCategories (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(categoryService.getAllCategoies(page, size));
    }


}
