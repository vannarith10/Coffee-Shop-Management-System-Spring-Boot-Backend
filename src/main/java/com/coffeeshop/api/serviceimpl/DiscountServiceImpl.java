package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.dto.discount.DiscountRequest;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final ProductRepository productRepository;
    private static final ZoneId KH_ZONE = ZoneId.of("Asia/Phnom_Penh");

    @Override
    public void applyDiscount(DiscountRequest request) {
        // Find product
        Product product = productRepository.findById(request.productId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found."));

        // Today time
        LocalDate today = LocalDate.now(KH_ZONE);

        // End date must not be in the past
        if(request.endDate().isBefore(today)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must not be in the past.");
        }

        // End date must be >= start date
        if(request.endDate().isBefore(request.startDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        // Rate must not be NULL
        if(request.rate() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount rate cannot be null.");
        }

        // Rate must be between 0 and 1
        if(request.rate().compareTo(BigDecimal.ZERO) < 0 || request.rate().compareTo(BigDecimal.ONE) > 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount rate must be between 0 and 1.");
        }

        // Convert date
        LocalDateTime start = request.startDate().atStartOfDay(KH_ZONE).toLocalDateTime();
        LocalDateTime end = request.endDate().atStartOfDay(KH_ZONE).toLocalDateTime();

        // Save
        product.setDiscountRate(request.rate());
        product.setDiscountStartDate(start);
        product.setDiscountEndDate(end);
        product.setDiscount(true);

        productRepository.save(product);
    }
}
