package com.coffeeshop.api.dto.payment_test;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GenerateQrRequest(
        String req_time,
        String merchant_id,
        String tran_id,
        BigDecimal amount,
        String currency,
        String payment_option,
        int lifetime,
        String qr_image_template,
        String hash
) {}

