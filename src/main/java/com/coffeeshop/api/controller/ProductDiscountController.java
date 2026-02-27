package com.coffeeshop.api.controller;

import com.coffeeshop.api.dto.discount.DiscountRequest;
import com.coffeeshop.api.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discounts")
public class ProductDiscountController {

    private final DiscountService discountService;

    @PostMapping
    public ResponseEntity<?> applyDiscount (@RequestBody DiscountRequest request) {
        discountService.applyDiscount(request);
        return ResponseEntity.ok("Discount applied successfully.");
    }

}
