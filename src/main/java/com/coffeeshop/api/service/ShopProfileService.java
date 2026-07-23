package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.setting.GetShopNameAndImage;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopProfile;
import com.coffeeshop.api.dto.adminDashboard.setting.ShopLogoUpdateResponse;
import com.coffeeshop.api.dto.adminDashboard.setting.UpdateShopProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ShopProfileService {

    GetShopProfile getShopProfile ();

    GetShopProfile updateShopProfilePartially (UpdateShopProfileRequest request, MultipartFile image);

    GetShopNameAndImage getShopNameAndImage ();

    void deleteShopLogo ();

    ShopLogoUpdateResponse updateShopLogo (MultipartFile image);

}
