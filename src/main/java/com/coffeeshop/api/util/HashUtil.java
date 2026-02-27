package com.coffeeshop.api.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class HashUtil {

    public static String generateHash(String data, String apiKey) {
        try {
            Mac hmacSha512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    apiKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );

            hmacSha512.init(secretKey);

            byte[] hashBytes = hmacSha512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}
