package com.coffeeshop.api.domain.enums;

public enum PaymentStatus {
    PENDING,         // Payment initiated, waiting for customer action
    COMPLETED,       // Payment successfully received and confirmed
    PAID,
    APPROVED,
    FAILED,          // Payment attempt failed (e.g. insufficient funds, rejected)
    CANCELLED,       // Transaction stopped by user or staff
    EXPIRED,         // Payment window timed out (mainly for ABA QR)
    REFUNDED,        // Full amount has been refunded
    PARTIALLY_REFUNDED  // Only part of the amount was refunded
}

