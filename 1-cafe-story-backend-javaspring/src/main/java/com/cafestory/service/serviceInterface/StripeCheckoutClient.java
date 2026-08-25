package com.cafestory.service.serviceInterface;

import com.cafestory.entity.Payment;

public interface StripeCheckoutClient {

    StripeCheckoutSession createCheckoutSession(Payment payment);

    String getSessionStatus(String sessionId);

    void expireSession(String sessionId);

    void refundPaymentIntent(String paymentIntentId);

    String getPaymentIntentId(String sessionId);

    record StripeCheckoutSession(String sessionId, String paymentUrl, String rawResponse) {
    }
}
