package com.coffeeshop.api.security;


import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthorizationGuard {

    private final UserRepository userRepository;
    private final UserAuthService userAuthService;


    // ROLE VALIDATION AND RETURN USER
    public User requireRole (Role role) {
        User user = userRepository.findById(userAuthService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found."));

        if (user.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Required role: " + role.name());
        }
        return user;
    }


    // USAGES
    public User requireAdmin () {
        return requireRole(Role.ADMIN);
    }
    public User requireCashier () {
        return requireRole(Role.CASHIER);
    }
    public User requireBarista () {
        return requireRole(Role.BARISTA);
    }


    // MANY ROLES VALIDATION AND RETURN USER
    public User requireAnyRoles (Role... roles) {
        User user = userRepository.findById(userAuthService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found."));

        if (Arrays.stream(roles).noneMatch(role -> role == user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Insufficient permissions.");
        }
        return user;
    }



}
