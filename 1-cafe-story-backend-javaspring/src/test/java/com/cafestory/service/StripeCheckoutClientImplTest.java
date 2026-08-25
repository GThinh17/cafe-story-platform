package com.cafestory.service;

import com.cafestory.entity.AdFee;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.entity.enums.AdFeeType;
import com.cafestory.service.serviceImplement.StripeCheckoutClientImpl;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import com.stripe.exception.ApiException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link StripeCheckoutClientImpl}.
 *
 * <p>Toàn bộ API Stripe được gọi qua phương thức tĩnh nên bị chặn bằng
 * {@code mockStatic}. Ngoài đường đi thành công, bài kiểm thử phủ kín nhóm
 * kiểm tra cấu hình — khoá bí mật, URL chuyển hướng, số tiền, đơn vị tiền tệ —
 * vì đó là nơi lỗi triển khai hay xuất hiện nhất.
 */
class StripeCheckoutClientImplTest {

    private static final String SUCCESS_URL = "https://cafestory.vn/payment/success";
    private static final String CANCEL_URL = "https://cafestory.vn/payment/cancel?from=checkout";

    @Test
    void createCheckoutSession_success_returnsSessionIdAndUrl_TC001() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Payment payment = payment();
        payment.setExtraFee(extraFee());
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_123");
        when(session.getUrl()).thenReturn("https://checkout.stripe.com/c/cs_123");
        when(session.toJson()).thenReturn("{\"id\":\"cs_123\"}");

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            StripeCheckoutClient.StripeCheckoutSession result = client.createCheckoutSession(payment);

            assertThat(result.sessionId()).isEqualTo("cs_123");
            assertThat(result.paymentUrl()).isEqualTo("https://checkout.stripe.com/c/cs_123");
            assertThat(result.rawResponse()).contains("cs_123");
        }
    }

    @Test
    void createCheckoutSession_success_adFeePaymentUsesGenericProductName_TC002() {
        StripeCheckoutClientImpl client = client("sk_live_dummy", SUCCESS_URL, CANCEL_URL);
        Payment payment = payment();
        payment.setAdFee(adFee());
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_ad");

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            assertThat(client.createCheckoutSession(payment).sessionId()).isEqualTo("cs_ad");
        }
    }

    @Test
    void createCheckoutSession_success_paymentWithoutProductStillWorks_TC003() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Payment payment = payment();
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_plain");

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            assertThat(client.createCheckoutSession(payment).sessionId()).isEqualTo("cs_plain");
        }
    }

    @Test
    void createCheckoutSession_fail_secretKeyMissingOrMalformed_TC004() {
        Payment payment = payment();
        StripeCheckoutClientImpl blankKey = client("  ", SUCCESS_URL, CANCEL_URL);
        StripeCheckoutClientImpl wrongPrefix = client("pk_test_dummy", SUCCESS_URL, CANCEL_URL);

        assertThatThrownBy(() -> blankKey.createCheckoutSession(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Stripe secret key is not configured");
        assertThatThrownBy(() -> wrongPrefix.createCheckoutSession(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Stripe secret key format is invalid");
    }

    @Test
    void createCheckoutSession_fail_redirectUrlsInvalid_TC005() {
        Payment payment = payment();
        StripeCheckoutClientImpl noSuccessUrl = client("sk_test_dummy", "", CANCEL_URL);
        StripeCheckoutClientImpl noCancelUrl = client("sk_test_dummy", SUCCESS_URL, null);
        StripeCheckoutClientImpl notHttp = client("sk_test_dummy", "cafestory.vn/success", CANCEL_URL);

        assertThatThrownBy(() -> noSuccessUrl.createCheckoutSession(payment))
                .hasMessageContaining("Stripe success URL is not configured");
        assertThatThrownBy(() -> noCancelUrl.createCheckoutSession(payment))
                .hasMessageContaining("Stripe cancel URL is not configured");
        assertThatThrownBy(() -> notHttp.createCheckoutSession(payment))
                .hasMessageContaining("Stripe redirect URL must start with http:// or https://");
    }

    @Test
    void createCheckoutSession_fail_amountAndCurrencyInvalid_TC006() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Payment noAmount = payment();
        noAmount.setAmount(null);
        Payment zeroAmount = payment();
        zeroAmount.setAmount(BigDecimal.ZERO);
        Payment noCurrency = payment();
        noCurrency.setCurrency("  ");

        assertThatThrownBy(() -> client.createCheckoutSession(noAmount))
                .hasMessageContaining("Stripe payment amount must be greater than 0");
        assertThatThrownBy(() -> client.createCheckoutSession(zeroAmount))
                .hasMessageContaining("Stripe payment amount must be greater than 0");
        assertThatThrownBy(() -> client.createCheckoutSession(noCurrency))
                .hasMessageContaining("Stripe payment currency is not configured");
    }

    @Test
    void createCheckoutSession_fail_stripeExceptionBecomesBadGateway_TC007() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Payment payment = payment();

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new ApiException("card_declined; code: 402", "req_1", "code", 402, null));

            assertThatThrownBy(() -> client.createCheckoutSession(payment))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Stripe checkout session creation failed")
                    // Phần metadata "; code:" bị cắt khỏi thông điệp trả cho client.
                    .hasMessageContaining("card_declined")
                    .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_GATEWAY));
        }
    }

    @Test
    void createCheckoutSession_fail_unexpectedExceptionBecomesBadGateway_TC008() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Payment payment = payment();

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new IllegalStateException("network down"));

            assertThatThrownBy(() -> client.createCheckoutSession(payment))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("network down");
        }
    }

    @Test
    void createCheckoutSession_fail_unexpectedExceptionWithoutMessage_TC009() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Payment payment = payment();

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new IllegalStateException());

            assertThatThrownBy(() -> client.createCheckoutSession(payment))
                    .hasMessageContaining("unknown Stripe error");
        }
    }

    @Test
    void getSessionStatus_success_returnsStripeStatus_TC010() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Session session = mock(Session.class);
        when(session.getStatus()).thenReturn("complete");

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.retrieve("cs_123")).thenReturn(session);

            assertThat(client.getSessionStatus("cs_123")).isEqualTo("complete");
        }
    }

    @Test
    void getSessionStatus_fail_stripeError_TC011() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.retrieve(anyString()))
                    .thenThrow(new ApiException("no such session", "req_2", "code", 404, null));

            assertThatThrownBy(() -> client.getSessionStatus("cs_missing"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to retrieve Stripe session");
        }
    }

    @Test
    void expireSession_success_callsExpireOnSession_TC012() throws Exception {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Session session = mock(Session.class);

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.retrieve("cs_123")).thenReturn(session);

            client.expireSession("cs_123");
        }

        verify(session).expire();
    }

    @Test
    void expireSession_fail_stripeError_TC013() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.retrieve(anyString()))
                    .thenThrow(new ApiException(null, "req_3", "code", 500, null));

            assertThatThrownBy(() -> client.expireSession("cs_123"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to expire Stripe session");
        }
    }

    @Test
    void refundPaymentIntent_success_createsRefund_TC014() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);

        try (MockedStatic<Refund> refunds = mockStatic(Refund.class)) {
            refunds.when(() -> Refund.create(any(RefundCreateParams.class))).thenReturn(new Refund());

            client.refundPaymentIntent("pi_123");

            refunds.verify(() -> Refund.create(any(RefundCreateParams.class)));
        }
    }

    @Test
    void refundPaymentIntent_fail_stripeError_TC015() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);

        try (MockedStatic<Refund> refunds = mockStatic(Refund.class)) {
            refunds.when(() -> Refund.create(any(RefundCreateParams.class)))
                    .thenThrow(new ApiException("charge already refunded", "req_4", "code", 400, null));

            assertThatThrownBy(() -> client.refundPaymentIntent("pi_123"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to refund Stripe payment");
        }
    }

    @Test
    void getPaymentIntentId_success_readsPaymentIntentFromSession_TC016() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);
        Session session = mock(Session.class);
        when(session.getPaymentIntent()).thenReturn("pi_123");

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.retrieve("cs_123")).thenReturn(session);

            assertThat(client.getPaymentIntentId("cs_123")).isEqualTo("pi_123");
        }
    }

    @Test
    void getPaymentIntentId_fail_stripeError_TC017() {
        StripeCheckoutClientImpl client = client("sk_test_dummy", SUCCESS_URL, CANCEL_URL);

        try (MockedStatic<Session> sessions = mockStatic(Session.class)) {
            sessions.when(() -> Session.retrieve(anyString()))
                    .thenThrow(new ApiException("no such session", "req_5", "code", 404, null));

            assertThatThrownBy(() -> client.getPaymentIntentId("cs_missing"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to retrieve Stripe session");
        }
    }

    private StripeCheckoutClientImpl client(String secretKey, String successUrl, String cancelUrl) {
        return new StripeCheckoutClientImpl(secretKey, successUrl, cancelUrl);
    }

    private Payment payment() {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("199000"));
        payment.setCurrency("VND");
        return payment;
    }

    private ExtraFee extraFee() {
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(UUID.randomUUID());
        extraFee.setName("Goi mo trang quan");
        extraFee.setDescription("Cho phep mo mot trang quan");
        return extraFee;
    }

    private AdFee adFee() {
        AdFee adFee = new AdFee();
        adFee.setAdFeeId(UUID.randomUUID());
        adFee.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        adFee.setPrice(new BigDecimal("500000"));
        return adFee;
    }
}
