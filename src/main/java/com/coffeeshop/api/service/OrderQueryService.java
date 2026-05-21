package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.order.OrderMessageToBarista;

import java.util.List;

public interface OrderQueryService {

    List<OrderMessageToBarista> baristaGetsOrders ();

}
