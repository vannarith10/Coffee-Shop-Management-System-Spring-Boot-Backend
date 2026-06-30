package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;

    // Send to Barista
    public void publishToBarista (Object event) {
        simpMessagingTemplate.convertAndSend("/topic/barista", event);
    }

    // Send to All ADMINS
    // Non-Admin attempting to subscribe will be rejected by ChannelAuthInterceptor
    public void publishEmployeeUpdateToAllAdmins (Object event) {
        simpMessagingTemplate.convertAndSend("/topic/admin/update-employee-details", event);
    }

}
