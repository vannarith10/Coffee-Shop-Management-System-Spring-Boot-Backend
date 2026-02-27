package com.coffeeshop.api.dto.payment_test;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CallBackRequest(


        @JsonProperty("tran_id")
        @NotBlank(message = "tran_id is required")
        String tranId,

        @JsonProperty("status")
        @NotBlank(message = "status is required")
        String status,

        @JsonProperty("apv")
        String apv,

        @JsonProperty("return_params")
        String returnParam
) {
}
