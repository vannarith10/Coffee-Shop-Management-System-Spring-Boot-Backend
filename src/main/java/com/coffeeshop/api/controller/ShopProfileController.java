package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.adminDashboard.setting.GetShopNameAndImage;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopProfile;
import com.coffeeshop.api.dto.adminDashboard.setting.ShopLogoUpdateResponse;
import com.coffeeshop.api.dto.adminDashboard.setting.UpdateShopProfileRequest;
import com.coffeeshop.api.service.ShopProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/shop-profile")
public class ShopProfileController {

    private final ShopProfileService shopProfileService;



    // GET PROFILE
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetShopProfile> shopProfile () {
        return ResponseEntity.ok(shopProfileService.getShopProfile());
    }



    // UPDATE SHOP PROFILE
    @PatchMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetShopProfile> updateProfile (
            @RequestPart(value = "data", required = false) @Valid UpdateShopProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
            ) {
        return ResponseEntity.ok(shopProfileService.updateShopProfilePartially(request, image));
    }



    // GET SHOP NAME & LOGO
    // Allowed all
    @GetMapping("/shop-name/shop-image")
    public ResponseEntity<GetShopNameAndImage> getShopNameAndImage () {
        return ResponseEntity.ok(shopProfileService.getShopNameAndImage());
    }


    // consumes = MediaType.MULTIPART_FORM_DATA_VALUE ==>> Content-Type: multipart/form-data
    // "This endpoint only accepts requests with Content-Type: multipart/form-data."
    @PutMapping(value = "/update-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShopLogoUpdateResponse> updateShopLogo (
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.ok(shopProfileService.updateShopLogo(image));
    }


    // Delete Shop Logo
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/delete-logo")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLogo () {
        shopProfileService.deleteShopLogo();
    }

}
