package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {


    List<Product> findAllByDiscountEndDateBefore(LocalDateTime now);

    boolean existsByNameIgnoreCase(String name);

    Page<Product> findByAvailableTrue (Pageable pageable);

    Page<Product> findByCategoryType (CategoryType categoryType, Pageable pageable);

    Page<Product> findByCategoryTypeAndCategoryName (
            CategoryType categoryType,
            String categoryName,
            Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);




    @Query("""
        SELECT p FROM Product p
        ORDER BY
            p.createdAt ASC
    """)
    Page<Product> findAllProductStock (Pageable pageable);

}
