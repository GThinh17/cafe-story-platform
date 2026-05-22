package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.dto.responseDTO.VnpayReturnResponseDTO;
import com.cafestory.entity.enums.PaymentStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {

    PaymentResponseDTO createPayment(CreatePaymentRequestDTO request);

    PaymentResponseDTO getPayment(UUID paymentId);

    List<PaymentResponseDTO> getAllPayments(PaymentStatus paymentStatus);

    PaymentResponseDTO markBankTransferPaid(UUID paymentId);

    void handleStripeWebhook(String payload, String signatureHeader);

    VnpayReturnResponseDTO handleVnpayReturn(Map<String, String> params);

    VnpayIpnResponseDTO handleVnpayIpn(Map<String, String> params);
}
