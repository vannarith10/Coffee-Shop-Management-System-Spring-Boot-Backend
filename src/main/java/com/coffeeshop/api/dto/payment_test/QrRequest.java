package com.coffeeshop.api.dto.payment_test;

import java.math.BigDecimal;

public record QrRequest(
        Double amount,
        String currency
) {
}
