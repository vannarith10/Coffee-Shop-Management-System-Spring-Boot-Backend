package com.coffeeshop.api.domain;

import com.coffeeshop.api.domain.enums.CurrencyType;
import com.coffeeshop.api.domain.enums.PaymentMethod;
import com.coffeeshop.api.domain.enums.PaymentStatus;
import com.coffeeshop.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relation to the Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Who processed/received this payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;           // CASH, ABA_PAYWAY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;           // PENDING, COMPLETED, FAILED, CANCELLED, EXPIRED, REFUNDED...

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;              // Total amount due (order total)

    @Column(precision = 12, scale = 2)
    private BigDecimal receivedAmount;      // Actual amount customer paid

    @Column(precision = 12, scale = 2)
    private BigDecimal changeAmount;        // For cash payments only (if customer gave more)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;          // USD, KHR, ...

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;


    // ───────────────────────────────────────────────
    // ABA PayWay specific fields
    // ───────────────────────────────────────────────


    // Your internal unique reference sent to ABA
    // Usually: order number + suffix or payment UUID
    @Column(length = 100, unique = true)
    private String merchantTransactionRef;

    // ABA's transaction ID (returned after QR creation or in callback)
    @Column(length = 50, unique = true)
    private String tranId;

    private String apv; // approval code from PayWay

    //URL or base64 of the QR code image (for frontend display)
    @Column(columnDefinition = "TEXT")
    private String qrCodeImage;

    //Deep link / ABA Pay app link (optional - app-to-app payment)
    @Column(length = 500)
    private String paymentDeepLink;

    //When the QR was generated / payment attempt started
    private Instant initiatedAt;

    //When payment was confirmed (via callback or manual check)
    private Instant completedAt;

    //Last time we polled ABA Check Transaction API
    private Instant lastStatusCheckedAt;

    //Raw JSON response from ABA callback or Check API (very useful for debugging & reconciliation)
    @Column(columnDefinition = "TEXT")
    private String abaResponsePayload;

    // Reason if payment failed or was canceled
    @Column(length = 255)
    private String failureReason;



    public void markPaid(String apv) {
        this.status = PaymentStatus.PAID;
        this.apv = apv;
        this.updatedAt = Instant.now();
    }

    // Lifecycle hooks (optional but recommended)
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
