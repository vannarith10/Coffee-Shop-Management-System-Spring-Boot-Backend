package com.coffeeshop.api.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ExceptionResponse<AnyType>
        (
                String message,
                Integer status,
                LocalDateTime timestamp,
                AnyType detail
        )
{
}
