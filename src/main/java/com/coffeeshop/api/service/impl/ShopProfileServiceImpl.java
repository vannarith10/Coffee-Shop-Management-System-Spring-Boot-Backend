package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.ShopProfile;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopNameAndImage;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopProfile;
import com.coffeeshop.api.dto.adminDashboard.setting.UpdateShopProfileRequest;
import com.coffeeshop.api.mapper.ShopProfileMapper;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.ShopProfileRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.ShopProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class ShopProfileServiceImpl implements ShopProfileService {


    private final AuthorizationGuard authorizationGuard;
    private final ImageStorageService imageStorageService;
    private final ShopProfileRepository shopProfileRepository;
    private final ShopProfileMapper shopProfileMapper;


    //---------------------------
    // GET SHOP PROFILE DETAIL
    //---------------------------
    @Override
    public GetShopProfile getShopProfile() {
        User admin = authorizationGuard.requireAdmin();
        ShopProfile profile = admin.getShopProfile();
        return shopProfileMapper.toShopProfileResponseDto(profile);
    }



    //------------------------------------
    // UPDATE SHOP PROFILE PARTIALLY
    //------------------------------------
    @Transactional
    @Override
    public GetShopProfile updateShopProfilePartially(UpdateShopProfileRequest request, MultipartFile image) {
        User admin = authorizationGuard.requireAdmin();
        ShopProfile profile = admin.getShopProfile();

        boolean noField = request == null || Stream.of(request.name(), request.contact(), request.address(), request.description(), request.region())
                .allMatch(v -> v == null || v.isBlank());

        if (noField && (image == null || image.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need at least one field.");
        }

        if (request != null) {
            if (request.name() != null && !request.name().isBlank()) profile.setName(request.name().trim());
            if (request.contact() != null && !request.contact().isBlank()) profile.setContactNumber(request.contact().trim());
            if (request.address() != null && !request.address().isBlank()) profile.setAddress(request.address().trim());
            if (request.description() != null && !request.description().isBlank()) profile.setDescription(request.description().trim());
            if (request.region() != null && !request.region().isBlank()) profile.setRegion(request.region().trim());
        }

        if (image != null && !image.isEmpty()) {
            String oldKey = profile.getImageKey();
            String newKey = uploadImage(image, imageStorageService.shopProfileFolder());
            profile.setImageKey(newKey);
            if (oldKey != null) {
                try { imageStorageService.delete(oldKey); }
                catch (Exception e) { /* log warning */ }
            }
        }

        return shopProfileMapper.toShopProfileResponseDto(profile);
    }




    //--------------------------------
    // GET SHOP'S NAME AND IMAGE
    //--------------------------------
    @Override
    public GetShopNameAndImage getShopNameAndImage() {
        Optional<ShopProfile> profile = shopProfileRepository.findFirstByOrderByIdAsc();
        ShopProfile shopProfile = profile.orElse(null);

        return GetShopNameAndImage.builder()
                .name(shopProfile.getName())
                .imageUrl(imageStorageService.getImageUrl(shopProfile.getImageKey()))
                .build();
    }



    // UPLOAD SHOP IMAGE
    private String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;
        imageStorageService.ensureBucketExists();
        try {
            return imageStorageService.upload(file, folder);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image.", ex);
        }
    }
}
