package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentHistoryService {

    List<PaymentResponseDTO> getPayments(UUID requesterUserId, PaymentStatus paymentStatus);
}
