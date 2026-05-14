package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.ShopProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShopProfileRepository extends JpaRepository<ShopProfile, UUID> {

    Optional<ShopProfile> findFirstByOrderByIdAsc();

}
