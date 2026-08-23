package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.Pagination;
import com.coffeeshop.api.dto.order.BaristaOrderItem;
import com.coffeeshop.api.dto.order.BaristaOrderQueue;
import com.coffeeshop.api.helper.PaginationHelper;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {


    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AuthorizationGuard authorizationGuard;




    /**
     * Retrieves the paginated order queue for the Barista KDS.
     *
     * If no status is provided, orders are retrieved using the default
     * KDS status priority: QUEUED → PREPARING → DONE. When a status is
     * specified, only orders with that status are returned in LIFO order.
     *
     * The retrieved orders are mapped to {@link BaristaOrderItem} and
     * returned together with pagination metadata.
     *
     * @param page   the requested page number
     * @param size   the number of orders per page
     * @param status optional order status used to filter the queue
     * @return the paginated Barista Order Queue
     */
    @Override
    public BaristaOrderQueue getOrders(int page, int size, OrderStatus status) {
        authorizationGuard.requireBarista();

        // pagination
        Pageable pageable = PaginationHelper.of(page, size);

        Page<Order> orderPage;

        if (status == null) {
            // return all
            orderPage = orderRepository.findAllByStatusPriority(pageable);
        } else {
            // return by provided status
            orderPage = orderRepository.findByStatus(pageable, status);
        }

        // convert to list of Barista order item
        List<BaristaOrderItem> baristaOrderItems = orderPage.getContent()
                .stream()
                .map(orderMapper::toBaristaOrderItem)
                .toList();

        // build pagination
        var pagination = Pagination.builder()
                .page(orderPage.getNumber() + 1)
                .size(orderPage.getSize())
                .itemCount(orderPage.getNumberOfElements())
                .totalPages(orderPage.getTotalPages())
                .totalItems(orderPage.getTotalElements())
                .build();


        return BaristaOrderQueue.builder()
                .pagination(pagination)
                .baristaOrderItems(baristaOrderItems)
                .build();
    }
}
