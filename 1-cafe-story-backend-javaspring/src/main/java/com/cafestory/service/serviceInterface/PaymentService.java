package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentService {

    PaymentResponseDTO createPayment(CreatePaymentRequestDTO request);

    PaymentResponseDTO getPayment(UUID paymentId);

    PaymentResponseDTO markBankTransferPaid(UUID paymentId);

    void handleStripeWebhook(String payload, String signatureHeader);
}
