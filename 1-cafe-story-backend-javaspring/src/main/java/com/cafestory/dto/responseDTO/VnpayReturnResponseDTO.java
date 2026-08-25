package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PaymentStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class VnpayReturnResponseDTO {

    private UUID paymentId;

    private String status;

    private PaymentStatus paymentStatus;

    private String responseCode;

    private String transactionStatus;

    private String transactionNo;

    private String message;
}
