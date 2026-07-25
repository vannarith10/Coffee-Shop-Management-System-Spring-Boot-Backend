package com.coffeeshop.api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private static final String TOPIC_ADMIN = "/topic/admin";
    private static final String TOPIC_CASHIER = "/topic/cashier";
    private static final String TOPIC_BARISTA = "/topic/barista";
    private static final String TOPIC_PUBLIC = "/topic/public";

    // Send to Barista
    public void publishToBarista (Object event) {
        simpMessagingTemplate.convertAndSend("/topic/barista", event);
    }

    // Send to All ADMINS
    // Non-Admin attempting to subscribe will be rejected by ChannelAuthInterceptor
    public void publishEmployeeUpdateToAllAdmins (Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/update-employee-details", event);
    }

    // STOCK Update
    public void publishProductStockUpdateToAdmins (Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/stock-update", event);
    }

    // Category Update
    public void publishCategoryUpdateToAdmins (Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/category-update", event);
    }

    // Category Create | Send new Category
    public void publishCategoryCreateToAdmins (Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/category-create", event);
    }

    // Category Create | Send new Status
    public void publishCategoryStatusSummaryToAdmins(Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/category-status-summary", event);
    }

    // Product Update
    public void publishProductUpdateToAdmins (Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/product-update", event);
    }


    // Staff Create
    public void publishCreateStaffToAdmins (Object event) {
        simpMessagingTemplate.convertAndSend(TOPIC_ADMIN + "/staff-create", event);
    }

}
