package com.coffeeshop.api.controller.payment_test;


import com.coffeeshop.api.dto.payment_test.CheckTransactionResponse;
import com.coffeeshop.api.dto.payment_test.GenerateQrResponse;
import com.coffeeshop.api.dto.payment_test.QrRequest;
import com.coffeeshop.api.serviceimpl.payment_test.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;


    //===============================================================================================
    //                                  GENERATE QR CODE
    //===============================================================================================
    @PostMapping("/generate-qr")
    public ResponseEntity<GenerateQrResponse> generateQr(@Valid @RequestBody CreateQrRequestBody body) {

        // Map controller request -> your existing QrRequest record
        QrRequest req = new QrRequest(
                body.amount(),
                body.currency()
        );

        GenerateQrResponse response = paymentService.generateQr(req);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/check-transaction/{tranId}")
    public ResponseEntity<CheckTransactionResponse> checkTran (@PathVariable String tranId) {
        CheckTransactionResponse response = paymentService.checkTransaction(tranId);
        return ResponseEntity.ok().body(response);
    }


    // DTO
    public record CreateQrRequestBody(
            @NotNull @Positive
            Double amount,

            @NotNull @Size(min = 3, max = 3)
            String currency
    ) {}
}

