package com.coffeeshop.api.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

// MinIO

@Configuration
public class S3Config {

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.region}")
    private String region;

    @Value("${minio.internal-endpoint}")
    private String internalEndpoint;

    @Value("${minio.public-endpoint}")
    private String publicEndpoint;


    private StaticCredentialsProvider credentialsProvider () {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }


    private S3Configuration s3PathStyle () {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }


    @Bean
    public S3Client s3Client () {
        return S3Client.builder()
                .endpointOverride(URI.create(internalEndpoint))
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(s3PathStyle())
                .build();
    }


    @Bean
    public S3Presigner s3Presigner () {
        return S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint))
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(s3PathStyle())
                .build();
    }

}
