package com.coffeeshop.api.dto.payment_test;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CheckTransactionRequest(
        @JsonProperty("req_time") String reqTime,
        @JsonProperty("merchant_id") String merchantId,
        @JsonProperty("tran_id") String tranId,
        @JsonProperty("hash") String hash
) {}


//public record CheckTransactionRequest(
//        String req_time,
//        String merchant_id,
//        String tran_id,
//        String hash
//) {}

