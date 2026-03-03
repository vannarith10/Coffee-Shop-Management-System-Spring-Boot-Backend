package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void publishToAdminDashboardEvent (Object event) {
        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", event);
    }

    public void publishToBarista (Object event) {
        simpMessagingTemplate.convertAndSend("/topic/barista", event);
    }

}
