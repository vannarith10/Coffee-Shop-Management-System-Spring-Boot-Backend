package com.coffeeshop.api.util;

import com.coffeeshop.api.domain.ShopSetting;
import com.coffeeshop.api.repository.ShopSettingRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultShopSettingInitializer implements ApplicationRunner {

    private final ShopSettingRepository shopSettingRepository;

    private static final Integer DEFAULT_UNIT_TARGET = 200;

    public void run(@NonNull ApplicationArguments args) {
        createDefaultShopSettingIfNotExists();
    }


    private void createDefaultShopSettingIfNotExists () {
        // Check if any setting exists
        if (shopSettingRepository.count() > 0) {
            return;
        }

        // Create one
        ShopSetting defaultSetting = ShopSetting.builder()
                .unitTarget(DEFAULT_UNIT_TARGET)
                .build();

        shopSettingRepository.save(defaultSetting);
    }

}
