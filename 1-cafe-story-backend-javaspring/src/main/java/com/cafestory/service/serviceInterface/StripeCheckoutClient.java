package com.cafestory.service.serviceInterface;

import com.cafestory.entity.Payment;

public interface StripeCheckoutClient {

    StripeCheckoutSession createCheckoutSession(Payment payment);

    record StripeCheckoutSession(String sessionId, String paymentUrl, String rawResponse) {
    }
}
