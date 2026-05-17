package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.auth.LoginRequest;
import com.coffeeshop.api.dto.auth.LoginResponse;
import com.coffeeshop.api.dto.auth.RegisterResponse;
import com.coffeeshop.api.service.UserAuthService;
import com.coffeeshop.api.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthService userAuthService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userAuthService.login(request));
    }

}
