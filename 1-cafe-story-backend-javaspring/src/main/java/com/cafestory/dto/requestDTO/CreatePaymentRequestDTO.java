package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PaymentMethod;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentRequestDTO {

    private UUID extraFeeId;

    private UUID adFeeId;

    private UUID cafePageId;

    @NotNull(message = "Payment method is mandatory")
    private PaymentMethod paymentMethod;

    @AssertTrue(message = "Exactly one of extraFeeId or adFeeId is required")
    public boolean isExactlyOneFeeSelected() {
        return (extraFeeId == null) != (adFeeId == null);
    }
}
