package com.coffeeshop.api.minio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.presign-exp-seconds}")
    private int presignExpSeconds;


    public void ensureBucketExists () {
        try{
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }



    // Upload File
    public String upload(MultipartFile file, String folder) throws IOException {
        // Determine content type for correct browser rendering
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        // Create a key like: users/123/UUID.jpg
        String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "";
        };
        String prefix = (folder != null && !folder.isBlank()) ? folder.replaceAll("^/|/$", "") + "/" : "";
        String key = prefix + UUID.randomUUID() + (ext.isEmpty() ? "" : ("." + ext));

        // Upload the object with content type
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
        return key;
    }




    // Get File
    public URL getPresignedGetUrl(String key) {
        // Generates a time-limited URL for clients to read the image directly from MinIO
        var get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        var req = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpSeconds))
                .getObjectRequest(get)
                .build();

        return s3Presigner.presignGetObject(req).url();
    }



    // Get Image URL as String
    public String getImageUrl (String key) {
        return key != null ? getPresignedGetUrl(key).toString() : null;
    }



    // Delete Image by Key
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return; // nothing to delete
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception ex) {
            // log only, do NOT throw
            log.warn("Failed to delete S3 object: {}", key, ex);
        }
    }


    // Copy & Paste it from ProductServiceImpl on 10-May-2026
    // Normalizes folder path: products/<category-slug> or products/uncategorized
    public String buildFolder(String categoryName) {
        String slug = slugify(categoryName);
        if (slug.isBlank()) slug = "uncategorized";
        return "products/" + slug;
    } // Final return must be "products/..." or "products/uncategorized"

    // Simple slugifier for folders
    public String slugify(String input) {
        if (input == null) return "";
        String s = input.toLowerCase(Locale.ROOT).trim();
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("(^-+)|(-+$)", "");
        return s;
    }

    // For user image
    public String employeeFolder () {
        return "employees/";
    }

    public String shopProfileFolder () {
        return "shop/profile/";
    }

}
