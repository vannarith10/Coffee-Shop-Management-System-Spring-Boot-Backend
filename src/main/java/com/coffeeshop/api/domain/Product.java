package com.coffeeshop.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // selling price to customer

    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice; // how much price it cost (Milk + coffee + cup + ice)

    @Column(length = 200)
    private String description;

    private String imageUrl;

    private String imageKey;


    // For discount
    @Column(precision = 5, scale = 2)
    private BigDecimal discountRate; // 0.05 = 5%
    private LocalDateTime discountStartDate;
    private LocalDateTime discountEndDate;
    private boolean discount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean available; // True = In stock

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;
}

