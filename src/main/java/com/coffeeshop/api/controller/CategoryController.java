package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.category.CategoryResponse;
import com.coffeeshop.api.dto.category.CreateCategoryRequest;
import com.coffeeshop.api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
