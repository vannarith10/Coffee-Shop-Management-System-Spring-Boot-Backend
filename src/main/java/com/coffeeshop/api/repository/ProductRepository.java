package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {


    List<Product> findAllByDiscountEndDateBefore(LocalDateTime now);

    boolean existsByNameIgnoreCase(String name);

}
