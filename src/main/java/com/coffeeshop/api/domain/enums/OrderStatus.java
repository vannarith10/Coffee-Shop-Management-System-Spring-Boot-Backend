package com.coffeeshop.api.domain.enums;

public enum OrderStatus {


    CREATED,           // order created
    QUEUED,            // after order confirmed by cashier
    PREPARING,         // barista start preparing
    DONE,              // barista completed order
    //
    PAYMENT_PENDING,   // waiting for QR pay
    CANCELLED

    // For now, we are using only CREATED, QUEUE, PREPARING, and DONE
}

