package com.coffeeshop.api.dto.payment_test;

public record GenerateQrResponse(
        String qrString,
        String qrImage,
        String abapay_deeplink,
        String app_store,
        String play_store,
        String amount,
        String currency,
        Status status
) {
    public record Status(
            String code,
            String message,
            String trace_id
    ) {}
}
