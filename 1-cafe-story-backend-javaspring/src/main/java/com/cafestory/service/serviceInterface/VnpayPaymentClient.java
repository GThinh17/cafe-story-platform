package com.cafestory.service.serviceInterface;

import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;

import java.util.Map;

public interface VnpayPaymentClient {

    String createPaymentUrl(Payment payment, ExtraFee extraFee);

    String secureHash(Map<String, String> params);

    boolean verifySignature(Map<String, String> params);
}
