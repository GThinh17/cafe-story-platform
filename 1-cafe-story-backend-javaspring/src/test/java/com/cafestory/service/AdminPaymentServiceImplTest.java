package com.cafestory.service;

import com.cafestory.entity.Payment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.service.serviceImplement.AdminPaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDetailRepository paymentDetailRepository;

    @InjectMocks
    private AdminPaymentServiceImpl adminPaymentService;

    @Test
    void markBankTransferPaid_success_TC001() {
        Payment payment = payment(PaymentStatus.PENDING, PaymentMethod.BANK_TRANSFER);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId())).thenReturn(Optional.empty());

        var result = adminPaymentService.markBankTransferPaid(payment.getPaymentId());

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
        verify(paymentRepository).save(payment);
    }

    @Test
    void refundPayment_fail_notPaidConflict_TC002() {
        Payment payment = payment(PaymentStatus.PENDING, PaymentMethod.BANK_TRANSFER);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> adminPaymentService.refundPayment(payment.getPaymentId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    private Payment payment(PaymentStatus status, PaymentMethod method) {
        User buyer = new User();
        buyer.setUserId(UUID.randomUUID());
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBuyer(buyer);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(status);
        payment.setAmount(BigDecimal.valueOf(99000));
        payment.setCurrency("VND");
        return payment;
    }
}
