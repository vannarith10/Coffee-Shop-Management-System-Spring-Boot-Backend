package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.BaristaOrderService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.websocket.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BaristaOrderServiceImpl implements BaristaOrderService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;


    @Override
    public Order startPreparing(UUID orderId) {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate Role
        if(user.getRole() != Role.BARISTA){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only BARISTA can update.");
        }

        // Get Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));

        // Validate order status
        if(order.getStatus() != OrderStatus.QUEUED){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order must be in QUEUED state.");
        }

        // Update status & save
        order.setStatus(OrderStatus.PREPARING);
        order.setPreparationStartedAt(Instant.now());
        order.setProcessedBy(user);
        Order saved = orderRepository.save(order);

        // Real-time broadcast to all BARISTAS
        orderEventPublisher.sendToAllBaristas(OrderMapper.toOrderMessage(saved));

        return saved;
    }



    @Override
    public Order markDone(UUID orderId) {
        // Get User
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate Role
        if(user.getRole() != Role.BARISTA){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only BARISTA can update.");
        }

        // Get Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));

        // Check status
        if(order.getStatus() != OrderStatus.PREPARING){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order status must be in PREPARING state.");
        }

        // Update status & save
        order.setStatus(OrderStatus.DONE);
        order.setDoneAt(Instant.now());
        Order saved = orderRepository.save(order);

        // Send to all Baristas
        orderEventPublisher.sendToAllBaristas(OrderMapper.toOrderMessage(saved));

        return saved;
    }
}
