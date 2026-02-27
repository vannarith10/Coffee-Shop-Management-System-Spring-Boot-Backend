package com.coffeeshop.api.serviceimpl.payment_test;

import com.coffeeshop.api.config.payment_test.PaywayProperties;
import com.coffeeshop.api.dto.payment_test.CheckTransactionRequest;
import com.coffeeshop.api.dto.payment_test.CheckTransactionResponse;
import com.coffeeshop.api.dto.payment_test.GenerateQrResponse;
import com.coffeeshop.api.dto.payment_test.QrRequest;
import com.coffeeshop.api.util.HashUtil;
import com.coffeeshop.api.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${aba.apiKey}")
    private String apiKey;

    @Value("${aba.merchantId}")
    private String merchantId;

    @Value("${aba.generateQrUrl}")
    private String generateQrUrl;

    @Value("${aba.checkTransactionUrl}")
    private String checkTransactionUrl;

    private final RestTemplate restTemplate;
    private final OrderNumberGenerator orderNumberGenerator; // A001 - A002
    private final PaywayProperties paywayProperties;


    // Constants (as per PayWay QR API examples)
    private static final String PAYMENT_OPTION_ABA_KHQR = "abapay_khqr";
    private static final String DEFAULT_PURCHASE_TYPE = "purchase";
    private static final String DEFAULT_QR_TEMPLATE = "template3_color";
    private static final int DEFAULT_LIFETIME_MINUTES = 10;
    private static final DateTimeFormatter REQ_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter TRAN_DATE_FMT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    private static final DateTimeFormatter TRAN_ID_TIME_FMT = DateTimeFormatter.ofPattern("yyMMddHHmmss"); // 12 chars


    // Tran ID
    private static String generateTranId20() {
        String ts = ZonedDateTime.now(ZoneOffset.UTC).format(TRAN_ID_TIME_FMT); // 12
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase(); // 8
        return ts + suffix; // 20 chars total
    }


    // PayWay requires req_time in UTC format
    private String getReqTimeUtc() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(REQ_TIME_FMT);
    }

    // Handles null value
    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    //
    private static String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) return "";
        String cur = currency == null ? "" : currency.trim().toUpperCase();

        if ("KHR".equals(cur)) {
            return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
        }
        // Default to USD-style 2 decimals
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }


    private static String buildDataToHash(
            String reqTime,
            String merchantId,
            String tranId,
            String amount,
            String items,
            String firstName,
            String lastName,
            String email,
            String phone,
            String purchaseType,
            String paymentOption,
            String callbackUrl,
            String returnDeeplink,
            String currency,
            String customFields,
            String returnParams,
            String payout,
            Integer lifetime,
            String qrImageTemplate
    ) {
        return nvl(reqTime)
                + nvl(merchantId)
                + nvl(tranId)
                + nvl(amount)
                + nvl(items)
                + nvl(firstName)
                + nvl(lastName)
                + nvl(email)
                + nvl(phone)
                + nvl(purchaseType)
                + nvl(paymentOption)
                + nvl(callbackUrl)
                + nvl(returnDeeplink)
                + nvl(currency)
                + nvl(customFields)
                + nvl(returnParams)
                + nvl(payout)
                + (lifetime == null ? "" : String.valueOf(lifetime))
                + nvl(qrImageTemplate);
    }



    ////////////////////////////////////////////////////////////////////////
    //                         GENERATE QR CODE
    ////////////////////////////////////////////////////////////////////////
    public GenerateQrResponse generateQr(QrRequest request) {

        final String reqTime = getReqTimeUtc();

        // Format date as ddMMyyyy
        final String datePart = ZonedDateTime.now(ZoneOffset.UTC).format(TRAN_DATE_FMT);
        // {OrderNumber} + - + {ddMMyyyyHHmmss}
//        final String tranId = orderNumberGenerator.generate() + "-" + datePart;
        final String tranId = generateTranId20();

        // I want to see how tranId looks like
        System.out.println("Generated tranId = " + tranId);
        System.out.println("tranId length = " + tranId.length());



        // Required fields
        final String paymentOption = PAYMENT_OPTION_ABA_KHQR;
        final int lifetime = DEFAULT_LIFETIME_MINUTES;
        final String template = DEFAULT_QR_TEMPLATE;

        String rawCallbackUrl = paywayProperties.getCallbackPublicBaseUrl()
                + "/paywat/callback?token="
                + paywayProperties.getCallbackToken();

        String CALLBACK_URL = Base64
                .getEncoder()
                .encodeToString(rawCallbackUrl.getBytes(StandardCharsets.UTF_8));


        // Optional fields (not provided in your current QrRequest / service)
        // If later you want to provide these, set them here and in the payload.
        final String items = null;          // Base64 JSON (optional)
        final String firstName = null;      // optional
        final String lastName = null;       // optional
        final String email = null;          // optional
        final String phone = null;          // optional
        final String purchaseType = DEFAULT_PURCHASE_TYPE; // strongly recommended to avoid default ambiguity
        final String callbackUrl = CALLBACK_URL;    // Base64 (optional)
        final String returnDeeplink = null; // Base64 (optional)
        final String customFields = null;   // Base64 (optional)
        final String returnParams = null;   // optional (per your spec)
        final String payout = null;         // Base64 (optional)


        // Convert amount safely - From Double to BigDecimal
        BigDecimal amt = request.amount() == null ? null : BigDecimal.valueOf(request.amount());
        final String amountStr = formatAmount(amt, request.currency());


        // Build hash string in PayWay required order (including optional placeholders)
        final String dataToHash = buildDataToHash(
                reqTime,
                merchantId,
                tranId,
                amountStr,
                items,
                firstName,
                lastName,
                email,
                phone,
                purchaseType,
                paymentOption,
                callbackUrl,
                returnDeeplink,
                request.currency(),
                customFields,
                returnParams,
                payout,
                lifetime,
                template
        );
        final String hash = HashUtil.generateHash(dataToHash, apiKey);


        // Build request body.
        // Using Map to avoid DTO mismatches if your GenerateQrRequest record doesn't contain optional fields.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("req_time", reqTime);
        body.put("merchant_id", merchantId);
        body.put("tran_id", tranId);


        // Optional customer fields omitted (only add if you set them)
        // If we don't set, it won't add to body
        if (firstName != null) body.put("first_name", firstName);
        if (lastName != null) body.put("last_name", lastName);
        if (email != null) body.put("email", email);
        if (phone != null) body.put("phone", phone);


        body.put("amount", amountStr);
        body.put("currency", request.currency());
        body.put("purchase_type", purchaseType);
        body.put("payment_option", paymentOption);


        // Optional extras omitted (only add if you set them)
        // If we don't set, it won't add to body
        if (items != null) body.put("items", items);
        if (callbackUrl != null) body.put("callback_url", callbackUrl);
        if (returnDeeplink != null) body.put("return_deeplink", returnDeeplink);
        if (customFields != null) body.put("custom_fields", customFields);
        if (returnParams != null) body.put("return_params", returnParams);
        if (payout != null) body.put("payout", payout);


        body.put("lifetime", lifetime);
        body.put("qr_image_template", template);
        body.put("hash", hash);


        // POST
        GenerateQrResponse response = restTemplate.postForObject(generateQrUrl, body, GenerateQrResponse.class);

        if (response == null) {
            throw new IllegalStateException("Generate QR response is null for tranId = " + tranId);
        }

        return response;
    }



    //================================================================//
    //                      CHECK TRANSACTION
    //================================================================//
    public CheckTransactionResponse checkTransaction (String tranId) {
        final String reqTime = getReqTimeUtc();
        final String b4Hash = reqTime + merchantId + tranId;
        final String hash = HashUtil.generateHash(b4Hash, apiKey);

        CheckTransactionRequest payload = CheckTransactionRequest
                .builder()
                .reqTime(reqTime)
                .merchantId(merchantId)
                .tranId(tranId)
                .hash(hash)
                .build();

        //=================================================
        // TODO
        // Optional debug (remove after verified)
        System.out.println("reqTime=" + reqTime);
        System.out.println("merchantId=" + merchantId);
        System.out.println("tranId=" + tranId);
        System.out.println("b4Hash=" + b4Hash);
        System.out.println("hash=" + hash);
        //=================================================

        CheckTransactionResponse response = restTemplate.postForObject(checkTransactionUrl, payload, CheckTransactionResponse.class);

        if(response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check transaction response is null for tranId = " + tranId);
        }

        return response;
    }
}

