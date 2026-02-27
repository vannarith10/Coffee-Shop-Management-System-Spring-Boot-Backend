package com.coffeeshop.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "payment_callback_event",
        uniqueConstraints = @UniqueConstraint(name = "ux_callback_dedupe", columnNames = "dedupeKey")
)
public class PaymentCallbackEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String dedupeKey;

    @Column(nullable = false, length = 40)
    private String tranId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 50)
    private String apv;     // approval code from PayWay

    private Instant receivedAt = Instant.now();


}
