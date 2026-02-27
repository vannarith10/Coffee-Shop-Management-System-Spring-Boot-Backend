package com.coffeeshop.api.dto.payment_test;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

// @JsonPreperty tells Jackson which JSON field should map to which JAVA field
// Like we map from "payment_status_code" to "paymentStatusCode"
public record CheckTransactionResponse(
        @JsonProperty("data") Data data,
        @JsonProperty("status") Status status
) {
    public record Data(
            @JsonProperty("payment_status_code") Integer paymentStatusCode,
            @JsonProperty("total_amount") BigDecimal totalAmount,
            @JsonProperty("original_amount") BigDecimal originalAmount,
            @JsonProperty("refund_amount") BigDecimal refundAmount,
            @JsonProperty("discount_amount") BigDecimal discountAmount,
            @JsonProperty("payment_amount") BigDecimal paymentAmount,
            @JsonProperty("payment_currency") String paymentCurrency,
            @JsonProperty("apv") String apv,
            @JsonProperty("payment_status") String paymentStatus,
            @JsonProperty("transaction_date") String transactionDate
    ) {}

    public record Status(
            @JsonProperty("code") String code,
            @JsonProperty("message") String message,
            @JsonProperty("tran_id") String tranId
    ) {}
}

//
//public record CheckTransactionResponse(
//        DataBlock data,
//        Status status
//) {
//    public record DataBlock(
//            int payment_status_code,
//            double total_amount,
//            double original_amount,
//            double refund_amount,
//            double discount_amount,
//            double payment_amount,
//            String payment_currency,
//            String apv,
//            String payment_status,
//            String transaction_date
//    ) {}
//
//    public record Status(
//            String code,
//            String message,
//            String tran_id
//    ) {}
//}
//
