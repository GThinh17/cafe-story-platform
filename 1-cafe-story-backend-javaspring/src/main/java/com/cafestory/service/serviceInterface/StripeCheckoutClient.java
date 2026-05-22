package com.cafestory.service.serviceInterface;

import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;

public interface StripeCheckoutClient {

    StripeCheckoutSession createCheckoutSession(Payment payment, ExtraFee extraFee);

    record StripeCheckoutSession(String sessionId, String paymentUrl, String rawResponse) {
    }
}
