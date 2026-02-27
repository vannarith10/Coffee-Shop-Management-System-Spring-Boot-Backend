package com.coffeeshop.api.domain;

import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String orderNumber;           // A001, A002, A003

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;           // PREPARING, DONE

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;    // amount before discount/tax

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;    // Discount

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount;         // Tax

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;       // final amount

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // CAST, ABA_PAYWAY

    private Instant createdAt;
    private Instant confirmedAt;              // Complete payment & send to barista
    private Instant preparationStartedAt;     // when barista started working
    private Instant doneAt;                   // Barista completes the order
    private Instant cancelledAt;
    private Instant updatedAt;

    @Column(length = 500)
    private String note;

    @Column(length = 50)
    private String cancellationReason;    // "Customer changed mind", "Out of stock", etc.

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;               // who created the order (cashier)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private User processedBy;             // who completed it (barista)


    //========================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

