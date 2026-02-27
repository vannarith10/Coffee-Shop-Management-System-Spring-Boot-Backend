package com.coffeeshop.api.util;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountScheduler {

    private final ProductRepository productRepository;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Phnom_Penh")
    public void resetExpiredDiscounts() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Phnom_Penh"));

        List<Product> expiredProducts = productRepository.findAllByDiscountEndDateBefore(now);

        // Take a product from expiredProducts one by one
        for (Product product : expiredProducts) {
            product.setDiscountRate(null);
            product.setDiscountStartDate(null);
            product.setDiscountEndDate(null);
            product.setDiscount(false);
        }

        productRepository.saveAll(expiredProducts);
    }
}
