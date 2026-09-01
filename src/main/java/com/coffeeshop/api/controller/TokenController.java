package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.auth.RefreshAccessTokenRequest;
import com.coffeeshop.api.dto.auth.RefreshAccessTokenResponse;
import com.coffeeshop.api.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthTokenService authTokenService;


    @PostMapping("/refresh")
    public ResponseEntity<RefreshAccessTokenResponse> getAccessToken(
            @RequestBody
            @Validated
            RefreshAccessTokenRequest request){

        RefreshAccessTokenResponse response = authTokenService.generateAccessTokenFromRefreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

}
