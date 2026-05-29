package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.auth.LoginRequest;
import com.coffeeshop.api.dto.auth.LoginResponse;
import com.coffeeshop.api.dto.auth.RegisterResponse;
import com.coffeeshop.api.service.OAuth2CodeService;
import com.coffeeshop.api.service.UserAuthService;
import com.coffeeshop.api.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthService userAuthService;
    private final OAuth2CodeService oAuth2CodeService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userAuthService.login(request));
    }


    @PostMapping("/oauth2/callback")
    public ResponseEntity<LoginResponse> oauth2CallBack (@RequestParam String code) {
        LoginResponse response = oAuth2CodeService.retrieve(code)
                .orElseThrow(() -> new RuntimeException("Invalid or expired authorization code"));

        return ResponseEntity.ok(response);
    }


}
