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
    public ResponseEntity<Void> create (
            @Valid @RequestBody CreateCategoryRequest request
            ) {
        categoryService.createCategory(request);

        return ResponseEntity.ok().build();
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



    @GetMapping("/{id}")
    public GetAllCategoriesResponse.Category getCategoryById (
            @PathVariable UUID id
    ) {
        return categoryService.getCategoryById(id);
    }



    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> patchCategory (
            @PathVariable UUID id,
            @RequestBody PatchCategoryRequest request
            ) {
        categoryService.patchCategory(id, request);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/category-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryStatusResponse> getStatus () {
        return ResponseEntity.ok(categoryService.getCategoryStatus());
    }


    @GetMapping("/names")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<CategoryNameAndTypeResponse>> getAllNames () {
        return ResponseEntity.ok(categoryService.getAllCategoryNames());
    }


}
