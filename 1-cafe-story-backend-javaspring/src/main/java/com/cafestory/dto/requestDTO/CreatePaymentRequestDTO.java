package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentRequestDTO {

    @NotNull(message = "Extra fee id is mandatory")
    private UUID extraFeeId;

    @NotNull(message = "Payment method is mandatory")
    private PaymentMethod paymentMethod;
}
