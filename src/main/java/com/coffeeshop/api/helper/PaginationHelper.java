package com.coffeeshop.api.helper;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



@UtilityClass
public final class PaginationHelper {

    // returns the larger of the two values
    private int page (int page) {
        return Math.max(page - 1, 0);
    }

    // returns the value within a range
    private int size (int size) {
        return Math.clamp(size, 10, 50);
    }


    //
    public Pageable of (int page, int size) {
        return PageRequest.of(page(page), size(size));
    }

    // with sort
    public Pageable of (int page, int size, Sort sort) {
        return PageRequest.of(page(page), size(size), sort);
    }

}
