package com.coffeeshop.api.mapper;


import com.coffeeshop.api.domain.ShopProfile;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopProfile;
import com.coffeeshop.api.minio.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopProfileMapper {

    private final ImageStorageService imageStorageService;

    public GetShopProfile toShopProfileResponseDto (ShopProfile profile) {
        return GetShopProfile.builder()
                .name(profile.getName())
                .contact(profile.getContactNumber())
                .address(profile.getAddress())
                .description(profile.getDescription())
                .imageUrl(imageStorageService.getImageUrl(profile.getImageKey()))
                .region(profile.getRegion())
                .build();
    }
}
