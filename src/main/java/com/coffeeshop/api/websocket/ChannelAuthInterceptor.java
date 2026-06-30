package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ChannelAuthInterceptor implements ChannelInterceptor {

    private static final String ADMIN_TOPIC_PREFIX = "/topic/admin";


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

        // ROLE
        // Check if trying to subscribe to admin-only topic
        if (destination != null && destination.startsWith(ADMIN_TOPIC_PREFIX)) {
            Principal principal = accessor.getUser();
            Authentication auth = null;

            if (principal instanceof StompPrincipal stompPrincipal){
                auth = stompPrincipal.getAuthentication();
            }

            if(auth == null || !isAdmin(auth)){
                throw new IllegalArgumentException("Unauthorized: Admin access required for " + destination);
            }
        }

        return message;
    }


    //
    //  IS ADMIN CHECK
    //
    private boolean isAdmin(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities == null) {
            return false;
        }

        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }
}
