package com.coffeeshop.api.repository.payment_test;

import com.coffeeshop.api.domain.PaymentCallbackEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentCallbackEventRepository extends JpaRepository<PaymentCallbackEvent, UUID> {

    boolean existsByDedupeKey(String dedupeKey);

}
