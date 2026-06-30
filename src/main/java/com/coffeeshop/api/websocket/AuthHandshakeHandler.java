package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected @Nullable Principal determineUser(ServerHttpRequest request,
                                                WebSocketHandler wsHandler,
                                                Map<String, Object> attributes) {

        Object auth = attributes.get("SPRING.SECURITY.AUTHENTICATION");

        if (auth instanceof Authentication authentication) {
            return new StompPrincipal(
                    authentication.getName(),
                    authentication
            );
        }
        return request.getPrincipal();
    }


}
