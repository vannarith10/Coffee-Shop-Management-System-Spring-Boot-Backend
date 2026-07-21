package com.coffeeshop.api.helper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationHelper {

    private PaginationHelper () {}

    public static Pageable of(int page, int size) {
        int pg = page > 0 ? page - 1 : 0;
        int sz = Math.clamp(size, 10, 50);
        return PageRequest.of(pg, sz);
    }


    public static Pageable of(int page, int size, Sort sort) {
        int pag = page > 0 ? page - 1 : 0;
        int siz = Math.clamp(size, 10, 50);
        return PageRequest.of(pag, siz, sort);
    }

}
