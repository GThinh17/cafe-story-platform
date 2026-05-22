package com.cafestory.service.serviceImplement;

import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.service.serviceInterface.VnpayPaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class VnpayPaymentClientImpl implements VnpayPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(VnpayPaymentClientImpl.class);
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String VNPAY_VERSION = "2.1.0";

    private final String tmnCode;
    private final String hashSecret;
    private final String payUrl;
    private final String returnUrl;
    private final String ipnUrl;

    public VnpayPaymentClientImpl(
            @Value("${vnpay.tmn-code:}") String tmnCode,
            @Value("${vnpay.hash-secret:}") String hashSecret,
            @Value("${vnpay.pay-url:}") String payUrl,
            @Value("${vnpay.return-url:}") String returnUrl,
            @Value("${vnpay.ipn-url:}") String ipnUrl) {
        this.tmnCode = trim(tmnCode);
        this.hashSecret = trim(hashSecret);
        this.payUrl = trim(payUrl);
        this.returnUrl = trim(returnUrl);
        this.ipnUrl = trim(ipnUrl);
    }

    @Override
    public String createPaymentUrl(Payment payment, ExtraFee extraFee) {
        validateConfig();
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VNPAY_VERSION);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", vnpayAmount(payment));
        params.put("vnp_CurrCode", payment.getCurrency());
        params.put("vnp_TxnRef", payment.getPaymentId().toString());
        params.put("vnp_OrderInfo", "CafeStory payment " + payment.getPaymentId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", VNPAY_DATE_FORMAT.format(LocalDateTime.now()));
        if (payment.getExpiredAt() != null) {
            params.put("vnp_ExpireDate", VNPAY_DATE_FORMAT.format(payment.getExpiredAt()));
        }
        String unsignedQuery = toSignedQueryString(params);
        String secureHash = hmacSha512(unsignedQuery);
        log.debug("VNPAY payment URL returnUrl={}", returnUrl);
        log.debug("VNPAY payment URL ipnUrl={}", ipnUrl);
        log.debug("VNPAY payment URL sorted param keys={}", params.keySet());
        log.debug("VNPAY payment URL hashData={}", unsignedQuery);
        return payUrl + "?" + unsignedQuery + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public String secureHash(Map<String, String> params) {
        validateHashSecret();
        return hmacSha512(toSignedQueryString(params));
    }

    private String hmacSha512(String hashData) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac.doFinal(hashData.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create VNPAY secure hash");
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            return false;
        }
        return secureHash.equalsIgnoreCase(secureHash(params));
    }

    private void validateConfig() {
        validateHashSecret();
        validateRequired(tmnCode, "VNPAY TMN code is not configured");
        validateRequired(payUrl, "VNPAY pay URL is not configured");
        validateRequired(returnUrl, "VNPAY return URL is not configured");
        validateRequired(ipnUrl, "VNPAY IPN URL is not configured");
        validateHttpUrl(payUrl, "VNPAY pay URL must start with http:// or https://");
        validateHttpUrl(returnUrl, "VNPAY return URL must start with http:// or https://");
        validateHttpUrl(ipnUrl, "VNPAY IPN URL must start with http:// or https://");
    }

    private void validateHashSecret() {
        validateRequired(hashSecret, "VNPAY hash secret is not configured");
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateHttpUrl(String value, String message) {
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private String vnpayAmount(Payment payment) {
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VNPAY payment amount must be greater than 0");
        }
        return payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString();
    }

    private String toSignedQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> isSignable(entry.getKey(), entry.getValue()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private boolean isSignable(String key, String value) {
        return key != null
                && key.startsWith("vnp_")
                && !"vnp_SecureHash".equals(key)
                && !"vnp_SecureHashType".equals(key)
                && value != null
                && !value.isBlank();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
