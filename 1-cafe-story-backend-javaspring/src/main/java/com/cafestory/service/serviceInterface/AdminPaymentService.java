package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminPaymentService {

    Page<PaymentResponseDTO> getPayments(PaymentStatus paymentStatus, UUID buyerId, Pageable pageable);

    PaymentResponseDTO getPayment(UUID paymentId);

    PaymentResponseDTO markBankTransferPaid(UUID paymentId);

    PaymentResponseDTO refundPayment(UUID paymentId);

    int expireStalePayments();
}
