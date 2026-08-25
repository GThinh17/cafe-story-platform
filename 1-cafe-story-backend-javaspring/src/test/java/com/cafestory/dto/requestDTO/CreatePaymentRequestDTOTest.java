package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PaymentMethod;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePaymentRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_success_adFeeWithoutExtraFee_TC001() {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setAdFeeId(UUID.randomUUID());
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void validate_success_extraFeeWithoutAdFee_TC002() {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setExtraFeeId(UUID.randomUUID());
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void validate_fail_requiresExactlyOneFee_TC003() {
        CreatePaymentRequestDTO noFeeRequest = new CreatePaymentRequestDTO();
        noFeeRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        CreatePaymentRequestDTO bothFeesRequest = new CreatePaymentRequestDTO();
        bothFeesRequest.setExtraFeeId(UUID.randomUUID());
        bothFeesRequest.setAdFeeId(UUID.randomUUID());
        bothFeesRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        assertThat(validator.validate(noFeeRequest))
                .anySatisfy(violation -> assertThat(violation.getMessage())
                        .isEqualTo("Exactly one of extraFeeId or adFeeId is required"));
        assertThat(validator.validate(bothFeesRequest))
                .anySatisfy(violation -> assertThat(violation.getMessage())
                        .isEqualTo("Exactly one of extraFeeId or adFeeId is required"));
    }
}
