package com.coffeeshop.api.domain.enums;

public enum OrderStatus {

    CREATED,           // order created
    PAYMENT_PENDING,   // waiting for QR pay
    PREPARING,
    COMPLETED,
    CANCELLED,
    QUEUED,
    DONE

}

