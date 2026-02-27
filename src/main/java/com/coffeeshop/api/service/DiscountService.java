package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.discount.DiscountRequest;

public interface DiscountService {

    void applyDiscount (DiscountRequest request);

}
