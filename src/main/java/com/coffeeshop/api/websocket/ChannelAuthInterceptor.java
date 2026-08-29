package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChannelAuthInterceptor implements ChannelInterceptor {


    private static final String ADMIN_TOPIC_PREFIX = "/topic/admin";
    private static final String CASHIER_TOPIC_PREFIX = "/topic/cashier";
    private static final String BARISTA_TOPIC_PREFIX = "/topic/barista";


    private static final Map<String, String> TOPIC_ROLE_MAPPING = Map.of(
            ADMIN_TOPIC_PREFIX, "ADMIN",
            CASHIER_TOPIC_PREFIX, "CASHIER",
            BARISTA_TOPIC_PREFIX, "BARISTA"
    );



    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Only intercept SUBSCRIBE commands
        if (!StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String destination = accessor.getDestination();

        if (destination == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }

        Authentication auth = extractAuthentication(accessor);

        for (var entry : TOPIC_ROLE_MAPPING.entrySet()) {

            if (destination.startsWith(entry.getKey())) {

                String requiredRole = entry.getValue();

                if (auth == null || !hasRole(auth, requiredRole)) {
                    throw new IllegalArgumentException(
                            "Unauthorized: " + requiredRole +
                                    " access required for " + destination
                    );
                }

                break;
            }
        }

        return message;
    }




    private Authentication extractAuthentication(
            StompHeaderAccessor accessor) {

        Principal principal = accessor.getUser();

        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.getAuthentication();
        }

        return null;
    }



    // Role Check
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .anyMatch(authority ->
                        authority.equals(role)
                                || authority.equals("ROLE_" + role));
    }



}























