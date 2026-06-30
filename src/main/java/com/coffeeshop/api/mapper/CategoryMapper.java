package com.coffeeshop.api.mapper;


import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.dto.category.GetAllCategoriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {


    public GetAllCategoriesResponse.Category toCategoryResponse (Category category) {
        return GetAllCategoriesResponse.Category.builder()
                .categoryName(category.getName())
                .categoryType(category.getType())
                .isActive(category.isActive())
                .build();
    }

}
