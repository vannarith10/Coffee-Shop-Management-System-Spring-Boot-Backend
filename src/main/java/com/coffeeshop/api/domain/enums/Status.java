package com.coffeeshop.api.domain.enums;

public enum Status {
    ACTIVE,      // Staff is currently employed and can work/login
    INACTIVE,    // Account is disabled / staff is no longer employed (cannot log in)
    ON_LEAVE,    // Currently on approved leave (vacation, sick, etc.)
    SUSPENDED    // Temporarily suspended (disciplinary, pending review)
}