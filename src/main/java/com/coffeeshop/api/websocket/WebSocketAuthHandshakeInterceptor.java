package com.coffeeshop.api.websocket;

import com.coffeeshop.api.config.JwtService;
import com.coffeeshop.api.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;



    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if(!(request instanceof ServletServerHttpRequest servletServerHttpRequest)) {
            return true;
        }

        String token = servletServerHttpRequest.getServletRequest().getParameter("token");

        if(token == null || token.isBlank()){
            String header = servletServerHttpRequest.getServletRequest().getHeader("Authorization");
            if(header != null && header.startsWith("Bearer ")){
                token = header.substring(7).trim();
            }
        }

        if (token == null || token.isBlank()) {
            return true;
        }

        try{
            String username = jwtService.extractUsername(token);
            if (username == null || username.isBlank()){
                return true;
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if(!jwtService.isTokenValid(token, userDetails)) {
                return true;
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            attributes.put("SPRING.SECURITY.AUTHENTICATION", auth);
            SecurityContextHolder.getContext().setAuthentication(auth);

        }catch (Exception e){
            log.warn("WebSocket JWT authentication failed: {}", e.getMessage());
        }

        return true;
    }




    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               @Nullable Exception exception) {

    }
}
