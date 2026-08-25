package com.cafestory.service;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdFeeType;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.service.serviceImplement.AdminPaymentServiceImpl;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminPaymentServiceImpl}.
 *
 * <p>Phần nặng nhất là {@code expireStalePayments}: với thẻ Stripe, trạng thái
 * phiên quyết định kết cục — "complete" nghĩa là khách đã trả tiền nên phải hoàn
 * tiền, "open" thì đóng phiên, còn lại chỉ cập nhật cơ sở dữ liệu. Mọi lời gọi
 * Stripe đều đi qua {@link StripeCheckoutClient} nên được mock trọn vẹn.
 */
@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentDetailRepository paymentDetailRepository;
    @Mock
    private StripeCheckoutClient stripeCheckoutClient;

    private AdminPaymentServiceImpl adminPaymentService;

    private User buyer;

    @BeforeEach
    void setUp() {
        adminPaymentService = new AdminPaymentServiceImpl(
                paymentRepository, paymentDetailRepository, stripeCheckoutClient);
        buyer = buyer();
    }

    @Test
    void getPayments_success_joinsDetailsInOneBatch_TC001() {
        PageRequest pageable = PageRequest.of(0, 10);
        Payment payment = payment(PaymentMethod.BANK_TRANSFER, PaymentStatus.PENDING);
        payment.setExtraFee(extraFee());
        PaymentDetail detail = detail(payment);
        when(paymentRepository.findAdminPayments(PaymentStatus.PENDING, buyer.getUserId(), pageable))
                .thenReturn(new PageImpl<>(List.of(payment)));
        when(paymentDetailRepository.findByPaymentPaymentIdIn(List.of(payment.getPaymentId())))
                .thenReturn(List.of(detail));

        Page<PaymentResponseDTO> result =
                adminPaymentService.getPayments(PaymentStatus.PENDING, buyer.getUserId(), pageable);

        PaymentResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getBuyerId()).isEqualTo(buyer.getUserId());
        assertThat(dto.getBuyerUserName()).isEqualTo("an");
        assertThat(dto.getProductName()).isEqualTo("Goi mo trang quan");
        assertThat(dto.getPaymentUrl()).isEqualTo("https://pay.example.com/session");
        assertThat(dto.getQrCodeUrl()).isEqualTo("https://pay.example.com/qr.png");
        assertThat(dto.getTransferContent()).isEqualTo("CAFESTORY 123");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("199000"));
    }

    @Test
    void getPayments_success_emptyPageSkipsDetailQuery_TC002() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(paymentRepository.findAdminPayments(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(adminPaymentService.getPayments(null, null, pageable).getContent()).isEmpty();
        verify(paymentDetailRepository, never()).findByPaymentPaymentIdIn(any());
    }

    @Test
    void getPayment_success_adFeeProductNameAndNullBuyer_TC003() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PAID);
        payment.setBuyer(null);
        payment.setAdFee(adFee());
        payment.setCafePage(cafePage());
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.empty());

        PaymentResponseDTO result = adminPaymentService.getPayment(payment.getPaymentId());

        assertThat(result.getBuyerId()).isNull();
        assertThat(result.getBuyerUserName()).isNull();
        assertThat(result.getBuyerUserFullName()).isNull();
        assertThat(result.getBuyerUserAvatar()).isNull();
        assertThat(result.getProductName()).isEqualTo(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS.name());
        assertThat(result.getActivatedCafePageId()).isNotNull();
        assertThat(result.getPaymentUrl()).isNull();
    }

    @Test
    void getPayment_success_noProductLeavesProductNameNull_TC004() {
        Payment payment = payment(PaymentMethod.VNPAY, PaymentStatus.PENDING);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.empty());

        assertThat(adminPaymentService.getPayment(payment.getPaymentId()).getProductName()).isNull();
    }

    @Test
    void getPayment_fail_notFound_TC005() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminPaymentService.getPayment(paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void markBankTransferPaid_success_stampsPaidAt_TC006() {
        Payment payment = payment(PaymentMethod.BANK_TRANSFER, PaymentStatus.PENDING);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.empty());

        PaymentResponseDTO result = adminPaymentService.markBankTransferPaid(payment.getPaymentId());

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    void markBankTransferPaid_fail_notABankTransfer_TC007() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        UUID paymentId = payment.getPaymentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> adminPaymentService.markBankTransferPaid(paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment is not a bank transfer");
    }

    @Test
    void markBankTransferPaid_fail_alreadyRefunded_TC008() {
        Payment payment = payment(PaymentMethod.BANK_TRANSFER, PaymentStatus.REFUNDED);
        UUID paymentId = payment.getPaymentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> adminPaymentService.markBankTransferPaid(paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Refunded payment cannot be marked paid");
    }

    @Test
    void refundPayment_success_stripeCardCallsProviderRefund_TC009() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PAID);
        PaymentDetail detail = detail(payment);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponseDTO result = adminPaymentService.refundPayment(payment.getPaymentId());

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(stripeCheckoutClient).refundPaymentIntent("pi_123");
    }

    @Test
    void refundPayment_success_stripeCardWithoutTransactionIdSkipsProvider_TC010() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PAID);
        PaymentDetail detail = detail(payment);
        detail.setProviderTransactionId(null);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail));
        when(paymentRepository.save(payment)).thenReturn(payment);

        adminPaymentService.refundPayment(payment.getPaymentId());

        verify(stripeCheckoutClient, never()).refundPaymentIntent(any());
    }

    @Test
    void refundPayment_success_bankTransferNeverTouchesStripe_TC011() {
        Payment payment = payment(PaymentMethod.BANK_TRANSFER, PaymentStatus.PAID);
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(payment)).thenReturn(payment);

        adminPaymentService.refundPayment(payment.getPaymentId());

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(stripeCheckoutClient, never()).refundPaymentIntent(any());
    }

    @Test
    void refundPayment_fail_paymentIsNotPaid_TC012() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        UUID paymentId = payment.getPaymentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> adminPaymentService.refundPayment(paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only paid payments can be refunded");
    }

    // ------------------------------------------------- expireStalePayments

    @Test
    void expireStalePayments_success_completeSessionIsRefunded_TC013() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        PaymentDetail detail = detail(payment);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail));
        when(stripeCheckoutClient.getSessionStatus("cs_123")).thenReturn("complete");

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(stripeCheckoutClient).refundPaymentIntent("pi_123");
    }

    @Test
    void expireStalePayments_success_completeSessionWithoutIntentJustExpires_TC014() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        PaymentDetail detail = detail(payment);
        detail.setProviderTransactionId(null);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail));
        when(stripeCheckoutClient.getSessionStatus("cs_123")).thenReturn("complete");

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(stripeCheckoutClient, never()).refundPaymentIntent(any());
    }

    @Test
    void expireStalePayments_success_openSessionIsClosedAtStripe_TC015() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail(payment)));
        when(stripeCheckoutClient.getSessionStatus("cs_123")).thenReturn("open");

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(stripeCheckoutClient).expireSession("cs_123");
    }

    @Test
    void expireStalePayments_success_unknownSessionStatusOnlyUpdatesDatabase_TC016() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail(payment)));
        when(stripeCheckoutClient.getSessionStatus("cs_123")).thenReturn("expired");

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(stripeCheckoutClient, never()).expireSession(any());
    }

    @Test
    void expireStalePayments_success_stripePaymentWithoutSessionId_TC017() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        PaymentDetail detail = detail(payment);
        detail.setProviderOrderId(null);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail));

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    void expireStalePayments_success_stripePaymentWithoutDetailRow_TC018() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.empty());

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    void expireStalePayments_success_nonStripePaymentJustExpires_TC019() {
        Payment payment = payment(PaymentMethod.BANK_TRANSFER, PaymentStatus.PENDING);
        givenStale(payment);

        assertThat(adminPaymentService.expireStalePayments()).isEqualTo(1);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(paymentRepository).save(payment);
    }

    @Test
    void expireStalePayments_success_providerErrorIsSwallowedAndNotCounted_TC020() {
        Payment payment = payment(PaymentMethod.STRIPE_CARD, PaymentStatus.PENDING);
        givenStale(payment);
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()))
                .thenReturn(Optional.of(detail(payment)));
        doThrow(new IllegalStateException("stripe down"))
                .when(stripeCheckoutClient).getSessionStatus("cs_123");

        assertThat(adminPaymentService.expireStalePayments()).isZero();
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void expireStalePayments_success_nothingStaleReturnsZero_TC021() {
        when(paymentRepository.findByPaymentStatusAndExpiredAtBefore(
                org.mockito.ArgumentMatchers.eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThat(adminPaymentService.expireStalePayments()).isZero();
    }

    // ------------------------------------------------------------- Helpers

    private void givenStale(Payment payment) {
        when(paymentRepository.findByPaymentStatusAndExpiredAtBefore(
                org.mockito.ArgumentMatchers.eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(payment));
    }

    private User buyer() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("an");
        user.setUserFullName("Nguyen Van An");
        user.setUserAvatar("https://cdn.example.com/an.png");
        return user;
    }

    private Payment payment(PaymentMethod method, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBuyer(buyer);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(status);
        payment.setAmount(new BigDecimal("199000"));
        payment.setCurrency("VND");
        payment.setCreatedAt(LocalDateTime.now().minusHours(2));
        payment.setExpiredAt(LocalDateTime.now().minusHours(1));
        return payment;
    }

    private PaymentDetail detail(Payment payment) {
        PaymentDetail detail = new PaymentDetail();
        detail.setPaymentDetailId(UUID.randomUUID());
        detail.setPayment(payment);
        detail.setProviderName("stripe");
        detail.setProviderTransactionId("pi_123");
        detail.setProviderOrderId("cs_123");
        detail.setProviderPaymentUrl("https://pay.example.com/session");
        detail.setProviderQrCodeUrl("https://pay.example.com/qr.png");
        detail.setTransferContent("CAFESTORY 123");
        return detail;
    }

    private ExtraFee extraFee() {
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(UUID.randomUUID());
        extraFee.setName("Goi mo trang quan");
        return extraFee;
    }

    private AdFee adFee() {
        AdFee adFee = new AdFee();
        adFee.setAdFeeId(UUID.randomUUID());
        adFee.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        adFee.setPrice(new BigDecimal("500000"));
        return adFee;
    }

    private CafePage cafePage() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Story");
        return cafePage;
    }
}
