package com.cafestory.integration;

import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentStripeWebhookPostgresIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String STRIPE_WEBHOOK_SECRET = "whsec_test_not_used";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Test
    void stripeWebhook_success_completedCheckoutUpdatesPaymentAndDetail() throws Exception {
        Payment payment = pendingStripePayment("stripebuyer01", "cs_test_complete_01");
        String payload = checkoutCompletedPayload("cs_test_complete_01", "pi_test_paid_01");

        mockMvc.perform(post("/api/payments/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", stripeSignature(payload))
                        .content(payload))
                .andExpect(status().isOk());

        Payment updated = paymentRepository.findById(payment.getPaymentId()).orElseThrow();
        assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updated.getPaidAt()).isNotNull();

        PaymentDetail detail = paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()).orElseThrow();
        assertThat(detail.getProviderTransactionId()).isEqualTo("pi_test_paid_01");
        assertThat(detail.getRawResponse()).contains("checkout.session.completed");
    }

    @Test
    void stripeWebhook_fail_invalidSignatureDoesNotUpdatePayment() throws Exception {
        Payment payment = pendingStripePayment("stripebuyer02", "cs_test_invalid_01");
        String payload = checkoutCompletedPayload("cs_test_invalid_01", "pi_test_paid_02");

        mockMvc.perform(post("/api/payments/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=1,v1=invalid")
                        .content(payload))
                .andExpect(status().isBadRequest());

        Payment unchanged = paymentRepository.findById(payment.getPaymentId()).orElseThrow();
        assertThat(unchanged.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(unchanged.getPaidAt()).isNull();

        PaymentDetail detail = paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()).orElseThrow();
        assertThat(detail.getProviderTransactionId()).isNull();
        assertThat(detail.getRawResponse()).isEqualTo("{}");
    }

    private Payment pendingStripePayment(String username, String sessionId) {
        User buyer = new User();
        buyer.setUserName(username);
        buyer.setUserFullName("Stripe Buyer");
        buyer.setUserEmail(username + "@example.com");
        buyer.setUserPassword("encoded-password");
        buyer.setAccountStatus(true);
        buyer = userRepository.save(buyer);

        Payment payment = new Payment();
        payment.setBuyer(buyer);
        payment.setPaymentMethod(PaymentMethod.STRIPE_CARD);
        payment.setAmount(new BigDecimal("25000.00"));
        payment.setCurrency("VND");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        payment = paymentRepository.save(payment);

        PaymentDetail detail = new PaymentDetail();
        detail.setPayment(payment);
        detail.setProviderName("STRIPE");
        detail.setProviderOrderId(sessionId);
        detail.setProviderPaymentUrl("https://checkout.stripe.test/" + sessionId);
        detail.setRawResponse("{}");
        paymentDetailRepository.save(detail);

        return payment;
    }

    private String checkoutCompletedPayload(String sessionId, String paymentIntentId) {
        return """
                {
                  "id": "evt_test_checkout_completed",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "%s",
                      "object": "checkout.session",
                      "payment_intent": "%s"
                    }
                  }
                }
                """.formatted(sessionId, paymentIntentId);
    }

    private String stripeSignature(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(STRIPE_WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8)));
        return "t=" + timestamp + ",v1=" + signature;
    }
}
