package com.coffeeshop.api.controller;

import com.coffeeshop.api.dto.AccessTokenResponse;
import com.coffeeshop.api.service.AuthTokenService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthTokenService authTokenService;


    @PostMapping("/get-acceess-token")
    public ResponseEntity<AccessTokenResponse> getAccessToken(@RequestBody @Validated RefreshToken refreshToken){

        System.out.println("########## Received refresh request: " + refreshToken);
        System.out.println("########## Token value: " + refreshToken.token());

        AccessTokenResponse response = authTokenService.generateAccessTokenFromRefreshToken(refreshToken.token());
        return ResponseEntity.ok(response);
    }



    // ========== DTO ========== //
    public record RefreshToken (
            @NotBlank
            String token
    ) {}

}
