package com.coffeeshop.api.minio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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



    // -----------------------------------
    //
    // Upload Image
    //
    // -----------------------------------
    public String upload(MultipartFile file, String folder) throws IOException {
        byte[] compressedBytes = compressToTargetSize(file.getBytes());
        String contentType = "image/jpeg";
        String ext = "jpg";

        String prefix = (folder != null && !folder.isBlank()) ? folder.replaceAll("^/|/$", "") + "/" : "";
        String key = prefix + UUID.randomUUID() + "." + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(compressedBytes)
        );
        return key;
    }


    // -----------------------------------
    //
    // Image Compressor
    //
    // -----------------------------------
    private byte[] compressToTargetSize(byte[] originalBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (originalImage == null) {
            return originalBytes;
        }

        int maxBytes = 100 * 1024;

        if (originalImage.getColorModel().hasAlpha()) {
            BufferedImage rgbImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();
            originalImage = rgbImage;
        }

        double scale = 1.0;
        int maxDimension = Math.max(originalImage.getWidth(), originalImage.getHeight());
        if (maxDimension > 1200) {
            scale = 1200.0 / maxDimension;
        }

        float lowQuality = 0.1f;
        float highQuality = 1.0f;
        byte[] bestBytes = null;

        while (lowQuality <= highQuality) {
            float midQuality = lowQuality + (highQuality - lowQuality) / 2;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .scale(scale)
                    .outputFormat("jpg")
                    .outputQuality(midQuality)
                    .toOutputStream(baos);

            byte[] compressed = baos.toByteArray();

            if (compressed.length > maxBytes) {
                highQuality = midQuality - 0.05f;
            } else {
                bestBytes = compressed;
                lowQuality = midQuality + 0.05f;
            }

            if (highQuality - lowQuality < 0.05f) {
                break;
            }
        }
        return bestBytes != null ? bestBytes : originalBytes;
    }




    // -----------------------------------
    //
    // Get Image URL
    //
    // -----------------------------------
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


    // -----------------------------------
    //
    // Get Image URL as String
    //
    // -----------------------------------
    public String getImageUrl (String key) {
        return key != null ? getPresignedGetUrl(key).toString() : null;
    }



    // -----------------------------------
    //
    // Delete Image by Key
    //
    // -----------------------------------
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
