package com.coffeeshop.api.controller.payment_test;

import com.coffeeshop.api.dto.payment_test.CallBackRequest;
import com.coffeeshop.api.serviceimpl.payment_test.CallBackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payway")
public class CallBackController {

    private final CallBackService callBackService;

    @PostMapping("/callback")
    public ResponseEntity<Void> callback (
            @RequestParam("token") String token,
            @Valid @RequestBody CallBackRequest body
            ) {
        callBackService.handleAsync(token, body);
        return ResponseEntity.ok().build();
    }

}
