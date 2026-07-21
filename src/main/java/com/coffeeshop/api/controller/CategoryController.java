package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.category.*;
import com.coffeeshop.api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        return ResponseEntity.ok(categoryService.getAllCategories(page, size));
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> patchCategory (
            @PathVariable UUID id,
            @RequestBody PatchCategoryRequest request
            ) {
        return ResponseEntity.ok(categoryService.patchCategory(id, request));
    }


    @GetMapping("/category-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryStatusResponse> getStatus () {
        return ResponseEntity.ok(categoryService.getCategoryStatus());
    }


    @GetMapping("/names")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryNameAndTypeResponse>> getAllNames () {
        return ResponseEntity.ok(categoryService.getAllCategoryNames());
    }


}
