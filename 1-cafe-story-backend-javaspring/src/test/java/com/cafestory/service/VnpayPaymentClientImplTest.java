package com.cafestory.service;

import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceImplement.VnpayPaymentClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class VnpayPaymentClientImplTest {

    private static final UUID PAYMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private VnpayPaymentClientImpl client;

    @BeforeEach
    void setUp() {
        client = new VnpayPaymentClientImpl(
                "TESTCODE",
                "SECRETKEY",
                "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
                "http://localhost:8080/api/payments/vnpay/return",
                "http://localhost:8080/api/payments/vnpay/ipn");
    }

    @Test
    void secureHash_success_matchesKnownHmacSha512_TC001() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_TxnRef", "123");
        params.put("vnp_TmnCode", "TESTCODE");
        params.put("vnp_Amount", "100000");

        String result = client.secureHash(params);

        assertThat(result).isEqualTo("283f5f3e3e81c722fef6f35f0c9e284b941c33a9c6ca408f3b71df860ed4f3cf5287c5b4b7d2226b369c3fe8377bcbe61e64481c9cb92535ada84bef27ea47c1");
    }

    @Test
    void verifySignature_success_validSignature_TC002() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_TxnRef", "123");
        params.put("vnp_TmnCode", "TESTCODE");
        params.put("vnp_Amount", "100000");
        params.put("vnp_SecureHash", client.secureHash(params));

        assertThat(client.verifySignature(params)).isTrue();
    }

    @Test
    void verifySignature_fail_invalidSignature_TC003() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_TxnRef", "123");
        params.put("vnp_TmnCode", "TESTCODE");
        params.put("vnp_Amount", "100000");
        params.put("vnp_SecureHash", "invalid");

        assertThat(client.verifySignature(params)).isFalse();
    }

    @Test
    void createPaymentUrl_success_containsRequiredParamsAndSecureHash_TC004() {
        String result = client.createPaymentUrl(payment());

        assertThat(result).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        assertThat(result).contains("vnp_Amount=29900000");
        assertThat(result).contains("vnp_Command=pay");
        assertThat(result).contains("vnp_CurrCode=VND");
        assertThat(result).contains("vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fpayments%2Fvnpay%2Freturn");
        assertThat(result).contains("vnp_TmnCode=TESTCODE");
        assertThat(result).contains("vnp_TxnRef=" + PAYMENT_ID);
        assertThat(result).contains("vnp_Version=2.1.0");
        assertThat(result).contains("vnp_SecureHash=");
        assertThat(result).doesNotContain("vnp_SecureHashType");
        assertThat(result).doesNotContain("vnp_IpnUrl");
    }

    @Test
    void createPaymentUrl_success_paramsSortedAndHashMatchesUnsignedQuery_TC005() {
        String result = client.createPaymentUrl(payment());
        String query = result.substring(result.indexOf('?') + 1);
        String unsignedQuery = query.substring(0, query.indexOf("&vnp_SecureHash="));

        assertThat(unsignedQuery).startsWith("vnp_Amount=29900000&vnp_Command=pay&vnp_CreateDate=");
        assertThat(unsignedQuery).contains("&vnp_CurrCode=VND&vnp_ExpireDate=");
        assertThat(unsignedQuery).contains("&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=");
        assertThat(unsignedQuery).contains("&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fpayments%2Fvnpay%2Freturn&vnp_TmnCode=TESTCODE&vnp_TxnRef=");
        assertThat(unsignedQuery).endsWith("&vnp_Version=2.1.0");
        assertThat(query).endsWith("vnp_SecureHash=" + client.secureHash(decodeParams(unsignedQuery)));
    }

    @Test
    void createPaymentUrl_success_secureHashDoesNotIncludeUnsupportedIpnUrl_TC008() {
        String result = client.createPaymentUrl(payment());
        String query = result.substring(result.indexOf('?') + 1);
        String unsignedQuery = query.substring(0, query.indexOf("&vnp_SecureHash="));
        String actualHash = query.substring(query.indexOf("&vnp_SecureHash=") + "&vnp_SecureHash=".length());
        Map<String, String> signedParams = decodeParams(unsignedQuery);

        assertThat(signedParams).doesNotContainKey("vnp_IpnUrl");
        assertThat(actualHash).isEqualTo(client.secureHash(signedParams));
    }

    @Test
    void secureHash_success_excludesSecureHashAndSecureHashType_TC006() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_TxnRef", "123");
        params.put("vnp_TmnCode", "TESTCODE");
        params.put("vnp_Amount", "100000");
        String expected = client.secureHash(params);
        params.put("vnp_SecureHash", "tampered");
        params.put("vnp_SecureHashType", "HmacSHA512");

        String result = client.secureHash(params);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void constructor_success_trimsTmnCodeAndHashSecret_TC007() {
        VnpayPaymentClientImpl trimmedClient = new VnpayPaymentClientImpl(
                " TESTCODE ",
                " SECRETKEY ",
                " https://sandbox.vnpayment.vn/paymentv2/vpcpay.html ",
                " http://localhost:8080/api/payments/vnpay/return ",
                " http://localhost:8080/api/payments/vnpay/ipn ");

        String result = trimmedClient.createPaymentUrl(payment());

        assertThat(result).contains("vnp_TmnCode=TESTCODE");
        assertThat(result).doesNotContain("vnp_TmnCode=+TESTCODE+");
    }

    private Payment payment() {
        Payment payment = new Payment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setBuyer(new User());
        payment.setExtraFee(extraFee());
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(BigDecimal.valueOf(299000));
        payment.setCurrency("VND");
        payment.setCreatedAt(LocalDateTime.of(2026, 5, 20, 10, 0));
        payment.setExpiredAt(LocalDateTime.of(2026, 5, 20, 10, 30));
        return payment;
    }

    private ExtraFee extraFee() {
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        extraFee.setName("Reviewer package");
        extraFee.setFeeType(ExtraFeeType.REVIEWER_REGISTRATION);
        extraFee.setPrice(299000);
        extraFee.setDurationMonths(6);
        extraFee.setStatus(true);
        return extraFee;
    }

    private Map<String, String> decodeParams(String query) {
        return java.util.Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> decode(pair[0]),
                        pair -> pair.length == 2 ? decode(pair[1]) : "",
                        (first, second) -> second,
                        LinkedHashMap::new));
    }

    private String decode(String value) {
        return URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
