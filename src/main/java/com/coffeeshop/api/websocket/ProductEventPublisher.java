package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;

    // Using Object as parameter means this publisher can send any event type, not just stock updates.
    public void publish (Object event) {
        simpMessagingTemplate.convertAndSend("/topic/products", event);
    }

}
