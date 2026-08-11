package com.coffeeshop.api.controller;

import com.coffeeshop.api.dto.GetUserProfile;
import com.coffeeshop.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/user")
public class UserController {

    private final UserService userService;


    @GetMapping("/profile")
    public ResponseEntity<GetUserProfile> getProfile () {
        return ResponseEntity.ok().body(userService.getProfile());
    }

}
