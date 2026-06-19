package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponseDTO {

    private UUID paymentId;

    private UUID buyerId;
    private String buyerUserName;
    private String buyerUserAvatar;

    private UUID extraFeeId;
    private UUID adFeeId;
    private String productName;

    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus paymentStatus;

    private String paymentUrl;

    private String qrCodeUrl;

    private String transferContent;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime expiredAt;
}
