package com.coffeeshop.api.controller;

import com.coffeeshop.api.security.CustomUserDetails;
import com.coffeeshop.api.service.oldService.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'BARISTA')")
public class TEST {

    private final UserService userService;

    @GetMapping("/me")
    public MeResponse me() {

        return new MeResponse(
                userService.getCurrentUserId(),
                userService.getCurrentUsername());
    }


    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
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




    @GetMapping("/debug")
    public Map<String, Object> debug(Authentication auth) {
        Object principal = auth.getPrincipal();
        return Map.of(
                "principalType", principal.getClass().getName(),
                "isCustomUserDetails", principal instanceof CustomUserDetails,
                "authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }




    @GetMapping("/ip")
    public Map<String, String> getClientIp(HttpServletRequest request) {
        // X-Forwarded-For is set by proxies/load balancers
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
        // Take the first one (the actual client)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return Map.of("ip", ip);
    }

}
