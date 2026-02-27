package com.coffeeshop.api.config.payment_test;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aba")
public class PaywayProperties {

    private String apiKey;

    private String merchantId;

    // Full URL for check-transaction-2 endpoint
    private String checkTransactionUrl;

    // Your public callback secret token (for security)
    private String callbackToken;

    // Your public callback base url (ngrok or production domain)
    private String callbackPublicBaseUrl;

}
