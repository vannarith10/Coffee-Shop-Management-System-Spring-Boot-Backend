package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.ShopSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShopSettingRepository extends JpaRepository<ShopSetting, UUID> {

    // Get the single settings record (assuming only one exists)
    Optional<ShopSetting> findFirstByOrderByCreatedAtDesc();


    // Alternative: find any single record
    Optional<ShopSetting> findTopByOrderByIdAsc();

}
