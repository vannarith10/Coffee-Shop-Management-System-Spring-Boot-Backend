package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.DisableUserRequest;
import com.coffeeshop.api.dto.DisableUserResponse;
import com.coffeeshop.api.dto.EnableUserRequest;
import com.coffeeshop.api.dto.auth.NewPasswordRequest;
import com.coffeeshop.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PasswordChangeResponse> changePassword (@RequestBody @Validated NewPasswordRequest request) {
        userService.setNewPassword(request);

        PasswordChangeResponse body = new PasswordChangeResponse(
                "Password updated successfully",
                request.username()
        );
        return ResponseEntity.ok(body);
    }


    @PutMapping("/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisableResponse> disableUser (@RequestBody @Validated DisableUserRequest request){
        userService.disableUser(request);
        return ResponseEntity.ok(new DisableResponse(
                "User disabled successfully",
                request.username()));
    }


    @PutMapping("/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnableUserResponse> enableUser (@RequestBody @Validated EnableUserRequest request) {
        userService.enableUser(request);
        return ResponseEntity.ok(new EnableUserResponse(
                "User enabled successfully.",
                request.username()
        ));
    }



    @GetMapping("/get-all-disabled-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisableUserResponse>> getAllDisabledUsers () {
        List<DisableUserResponse> response = userService.getAllDisabledUsers();
        return ResponseEntity.ok(response);
    }



    // ========================= DTO ========================= //
    //
    public record PasswordChangeResponse(
            String message,
            String username
    ) {}
    //
    public record DisableResponse(
            String message,
            String username
    ) {}
    //
    public record EnableUserResponse(
            String message,
            String username
    ){}

}
