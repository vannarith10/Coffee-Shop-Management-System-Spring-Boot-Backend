package com.coffeeshop.api.minio;

import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        return key; // Save this in your DB if you need to reference the file later
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



    // Delete File
    public void delete(String key) {
        // Deletes the object by key
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }


}
