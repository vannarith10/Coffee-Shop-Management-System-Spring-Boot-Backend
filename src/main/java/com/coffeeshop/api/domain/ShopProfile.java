package com.coffeeshop.api.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "shop_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20)
    private String contactNumber;

    @Column(length = 100)
    private String address;

    @Column(length = 200)
    private String description;

    @Column(length = 150)
    private String imageKey;

    @Column(nullable = false, length = 50)
    private String region;  // e.g. Asia/Phnom_Penh
}
