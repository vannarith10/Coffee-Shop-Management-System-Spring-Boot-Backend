package com.coffeeshop.api.websocket;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.order.OrderMessageToBarista;
import com.coffeeshop.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;


    // Send new order to all Baristas
    public void sendToAllBaristas (OrderMessageToBarista message) {
        // Fetch all baristas
        List<User> baristas = userRepository.findByRole(Role.BARISTA);

        // Send to each barista privately
        for(User barista : baristas) {
            simpMessagingTemplate.convertAndSendToUser(
                    barista.getId().toString(),
                    "/queue/orders",
                    message
            );
        }
    }



    public void publishStatusUpdate (OrderMessageToBarista message) {
        simpMessagingTemplate.convertAndSend("/topic/orders", message);
    }
}
