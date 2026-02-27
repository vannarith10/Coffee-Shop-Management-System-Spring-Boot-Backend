package com.coffeeshop.api.controller;

import com.coffeeshop.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TEST {

    private final UserService userService;

    @GetMapping("/me")
    public MeResponse me() {

        return new MeResponse(
                userService.getCurrentUserId(),
                userService.getCurrentUsername());
    }


    @GetMapping("/admin")
    public MeResponse admin(){
        return new MeResponse(userService.getCurrentUserId(), userService.getCurrentUsername());
    }



    public record MeResponse(
            UUID userId,
            String username
    ) {}



    @PostMapping("/content-type")
    public Map<String, String> contentType(HttpServletRequest request) {
        return Map.of(
                "contentType", String.valueOf(request.getContentType())
        );
    }

}
