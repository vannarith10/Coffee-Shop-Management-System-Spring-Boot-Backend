package com.coffeeshop.api.domain.enums;

public enum OrderStatus {

    CREATED,           // order created
    QUEUED,
    PREPARING,
    DONE,
    PAYMENT_PENDING,   // waiting for QR pay
    CANCELLED

    // For now, we are using only CREATED, QUEUE, PREPARING, and DONE
}

