package com.coffeeshop.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle ResponseStatusException (your custom service errors)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse<String>> handleServiceException(ResponseStatusException ex) {
        ExceptionResponse<String> errorResponse = ExceptionResponse.<String>builder()
                .message("Service Error")
                .status(ex.getStatusCode().value())
                .timestamp(LocalDateTime.now())
                .detail(ex.getReason())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    // Handle validation errors (DTO @Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResponse<String> errorResponse = ExceptionResponse.<String>builder()
                .message("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .detail(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handle any unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse<String>> handleUnexpectedException(Exception ex) {
        ExceptionResponse<String> errorResponse = ExceptionResponse.<String>builder()
                .message("Unexpected Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .detail(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
