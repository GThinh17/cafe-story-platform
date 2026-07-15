package com.cafestory.service;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.service.serviceImplement.PaymentHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentHistoryServiceImplTest {

    private static final UUID BUYER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PAYMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDetailRepository paymentDetailRepository;

    private PaymentHistoryServiceImpl paymentHistoryService;

    @BeforeEach
    void setUp() {
        paymentHistoryService = new PaymentHistoryServiceImpl(paymentRepository, paymentDetailRepository);
    }

    @Test
    void getPayments_success_withoutFilter_mapsDetailAndOptionalProducts() {
        Payment detailed = payment(PAYMENT_ID);
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(UUID.randomUUID());
        extraFee.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        detailed.setExtraFee(extraFee);
        AdFee adFee = new AdFee();
        adFee.setAdFeeId(UUID.randomUUID());
        detailed.setAdFee(adFee);
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        detailed.setCafePage(cafePage);
        PaymentDetail detail = new PaymentDetail();
        detail.setProviderPaymentUrl("https://checkout.stripe.com/test");
        detail.setProviderQrCodeUrl("https://cdn.example.test/qr.png");
        detail.setTransferContent("CAFE_PAYMENT_123");

        Payment plain = payment(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        when(paymentRepository.findByBuyerUserIdOrderByCreatedAtDesc(BUYER_ID))
                .thenReturn(List.of(detailed, plain));
        when(paymentDetailRepository.findByPaymentPaymentId(detailed.getPaymentId()))
                .thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(plain.getPaymentId()))
                .thenReturn(Optional.empty());

        List<PaymentResponseDTO> result = paymentHistoryService.getPayments(BUYER_ID, null);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getBuyerId()).isEqualTo(BUYER_ID);
        assertThat(result.getFirst().getExtraFeeType()).isEqualTo(ExtraFeeType.CAFE_PAGE_OPENING);
        assertThat(result.getFirst().getAdFeeId()).isEqualTo(adFee.getAdFeeId());
        assertThat(result.getFirst().getActivatedCafePageId()).isEqualTo(cafePage.getId());
        assertThat(result.getFirst().getPaymentUrl()).isEqualTo("https://checkout.stripe.com/test");
        assertThat(result.getFirst().getQrCodeUrl()).isEqualTo("https://cdn.example.test/qr.png");
        assertThat(result.getFirst().getTransferContent()).isEqualTo("CAFE_PAYMENT_123");
        assertThat(result.get(1).getExtraFeeId()).isNull();
        assertThat(result.get(1).getAdFeeId()).isNull();
        assertThat(result.get(1).getActivatedCafePageId()).isNull();
        assertThat(result.get(1).getPaymentUrl()).isNull();
        verify(paymentRepository).findByBuyerUserIdOrderByCreatedAtDesc(BUYER_ID);
    }

    @Test
    void getPayments_success_withStatusFilter() {
        when(paymentRepository.findByBuyerUserIdAndPaymentStatusOrderByCreatedAtDesc(
                BUYER_ID,
                PaymentStatus.PAID)).thenReturn(List.of());

        List<PaymentResponseDTO> result = paymentHistoryService.getPayments(BUYER_ID, PaymentStatus.PAID);

        assertThat(result).isEmpty();
        verify(paymentRepository).findByBuyerUserIdAndPaymentStatusOrderByCreatedAtDesc(
                BUYER_ID,
                PaymentStatus.PAID);
    }

    private Payment payment(UUID paymentId) {
        User buyer = new User();
        buyer.setUserId(BUYER_ID);
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBuyer(buyer);
        payment.setPaymentMethod(PaymentMethod.STRIPE_CARD);
        payment.setAmount(BigDecimal.valueOf(299000));
        payment.setCurrency("VND");
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setCreatedAt(LocalDateTime.of(2026, 7, 15, 10, 0));
        payment.setPaidAt(LocalDateTime.of(2026, 7, 15, 10, 1));
        payment.setExpiredAt(LocalDateTime.of(2026, 7, 15, 11, 0));
        return payment;
    }
}
