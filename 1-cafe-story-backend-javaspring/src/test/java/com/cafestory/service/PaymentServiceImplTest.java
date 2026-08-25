package com.cafestory.service;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdFeeType;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.AdFeeRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ExtraFeeRepository;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceImplement.PaymentServiceImpl;
import com.cafestory.service.serviceImplement.StripeCheckoutClientImpl;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import com.cafestory.service.serviceInterface.VnpayPaymentClient;
import com.cafestory.validation.CafePageValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.ApiException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private final UUID buyerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID extraFeeId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID adFeeId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID paymentId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDetailRepository paymentDetailRepository;

    @Mock
    private AdFeeRepository adFeeRepository;

    @Mock
    private ExtraFeeRepository extraFeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewerRepository reviewerRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private PageMemberRepository pageMemberRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private StripeCheckoutClient stripeCheckoutClient;

    @Mock
    private VnpayPaymentClient vnpayPaymentClient;

    @Mock
    private CafePageValidator cafePageValidator;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                paymentDetailRepository,
                adFeeRepository,
                extraFeeRepository,
                userRepository,
                reviewerRepository,
                cafePageRepository,
                pageMemberRepository,
                roleRepository,
                userRoleAssignmentRepository,
                stripeCheckoutClient,
                vnpayPaymentClient,
                new ObjectMapper(),
                cafePageValidator,
                "");
    }

    @Test
    void createPayment_success_stripeCard_TC001() {
        User buyer = user();
        ExtraFee extraFee = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockPaymentSave();
        mockPaymentDetailSave();
        when(stripeCheckoutClient.createCheckoutSession(any(Payment.class)))
                .thenReturn(new StripeCheckoutClient.StripeCheckoutSession("cs_test_123",
                        "https://checkout.stripe.com/test", "{}"));

        PaymentResponseDTO result = paymentService.createPayment(buyerId, request(PaymentMethod.STRIPE_CARD));

        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.STRIPE_CARD);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(299000));
        assertThat(result.getPaymentUrl()).isEqualTo("https://checkout.stripe.com/test");
        assertThat(result.getTransferContent()).isNull();
    }

    @Test
    void createPayment_success_bankTransfer_TC002() {
        User buyer = user();
        ExtraFee extraFee = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockPaymentSave();
        mockPaymentDetailSave();

        PaymentResponseDTO result = paymentService.createPayment(buyerId, request(PaymentMethod.BANK_TRANSFER));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(result.getPaymentUrl()).isNull();
        assertThat(result.getTransferContent()).isEqualTo("CAFE_PAYMENT_" + paymentId);
    }

    @Test
    void createPayment_success_vnpay_TC012() {
        User buyer = user();
        ExtraFee extraFee = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockPaymentSave();
        mockPaymentDetailSave();
        when(vnpayPaymentClient.createPaymentUrl(any(Payment.class)))
                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=" + paymentId);

        PaymentResponseDTO result = paymentService.createPayment(buyerId, request(PaymentMethod.VNPAY));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.VNPAY);
        assertThat(result.getPaymentUrl()).contains("vnp_TxnRef=" + paymentId);
        assertThat(result.getTransferContent()).isEqualTo("CAFE_VNPAY_" + paymentId);
    }

    @Test
    void createPayment_success_adFeeUsesAdAmountAndCurrency_TC021() {
        User buyer = user();
        AdFee adFee = adFee(true);
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(adFeeRepository.findById(adFeeId)).thenReturn(Optional.of(adFee));
        mockPaymentSave();
        mockPaymentDetailSave();

        PaymentResponseDTO result = paymentService.createPayment(buyerId, adFeeRequest(PaymentMethod.BANK_TRANSFER));

        assertThat(result.getAdFeeId()).isEqualTo(adFeeId);
        assertThat(result.getExtraFeeId()).isNull();
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500000));
        assertThat(result.getCurrency()).isEqualTo("VND");
    }

    @Test
    void createPayment_fail_requiresExactlyOneFee_TC022() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(user()));
        CreatePaymentRequestDTO noFeeRequest = new CreatePaymentRequestDTO();
        noFeeRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, noFeeRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        CreatePaymentRequestDTO bothFeesRequest = request(PaymentMethod.BANK_TRANSFER);
        bothFeesRequest.setAdFeeId(adFeeId);

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, bothFeesRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createPayment_fail_inactiveAdFee_TC023() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(user()));
        when(adFeeRepository.findById(adFeeId)).thenReturn(Optional.of(adFee(false)));

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, adFeeRequest(PaymentMethod.BANK_TRANSFER)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createPayment_fail_buyerNotFound_TC003() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, request(PaymentMethod.STRIPE_CARD)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createPayment_fail_extraFeeNotFoundOrInactive_TC004() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(user()));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, request(PaymentMethod.STRIPE_CARD)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        when(extraFeeRepository.findById(extraFeeId))
                .thenReturn(Optional.of(extraFee(false, ExtraFeeType.REVIEWER_REGISTRATION)));

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, request(PaymentMethod.STRIPE_CARD)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getAllPayments_success_withoutStatusFilter_TC031() {
        Payment firstPayment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        Payment secondPayment = pendingPayment(PaymentMethod.BANK_TRANSFER,
                extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        secondPayment.setPaymentId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "ADMIN")).thenReturn(true);
        when(paymentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(firstPayment, secondPayment));
        when(paymentDetailRepository.findByPaymentPaymentId(firstPayment.getPaymentId()))
                .thenReturn(Optional.of(paymentDetail(firstPayment)));
        when(paymentDetailRepository.findByPaymentPaymentId(secondPayment.getPaymentId())).thenReturn(Optional.empty());

        List<PaymentResponseDTO> result = paymentService.getAllPayments(buyerId, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPaymentId()).isEqualTo(firstPayment.getPaymentId());
        assertThat(result.get(0).getPaymentUrl()).isEqualTo("https://checkout.stripe.com/test");
        assertThat(result.get(1).getPaymentId()).isEqualTo(secondPayment.getPaymentId());
        verify(paymentRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllPayments_success_withStatusFilter_TC032() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "ADMIN")).thenReturn(true);
        when(paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus.PENDING))
                .thenReturn(List.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId())).thenReturn(Optional.empty());

        List<PaymentResponseDTO> result = paymentService.getAllPayments(buyerId, PaymentStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository).findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus.PENDING);
    }

    @Test
    void getMyPayments_success_withoutFilter_mapsDetailAndOptionalProducts_TC033() {
        Payment detailed = pendingPayment(
                PaymentMethod.STRIPE_CARD,
                extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        detailed.setPaymentStatus(PaymentStatus.PAID);
        AdFee adFee = adFee(true);
        detailed.setAdFee(adFee);
        CafePage cafePage = cafePage(detailed.getBuyer());
        detailed.setCafePage(cafePage);
        PaymentDetail detail = paymentDetail(detailed);
        detail.setProviderQrCodeUrl("https://cdn.example.test/qr.png");
        detail.setTransferContent("CAFE_PAYMENT_123");

        Payment plain = pendingPayment(
                PaymentMethod.BANK_TRANSFER,
                extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        plain.setPaymentId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
        plain.setExtraFee(null);
        when(paymentRepository.findByBuyerUserIdOrderByCreatedAtDesc(buyerId))
                .thenReturn(List.of(detailed, plain));
        when(paymentDetailRepository.findByPaymentPaymentId(detailed.getPaymentId()))
                .thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(plain.getPaymentId()))
                .thenReturn(Optional.empty());

        List<PaymentResponseDTO> result = paymentService.getMyPayments(buyerId, null);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getBuyerId()).isEqualTo(buyerId);
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
        verify(paymentRepository).findByBuyerUserIdOrderByCreatedAtDesc(buyerId);
    }

    @Test
    void getMyPayments_success_withStatusFilter_TC034() {
        when(paymentRepository.findByBuyerUserIdAndPaymentStatusOrderByCreatedAtDesc(
                buyerId,
                PaymentStatus.PAID)).thenReturn(List.of());

        List<PaymentResponseDTO> result = paymentService.getMyPayments(buyerId, PaymentStatus.PAID);

        assertThat(result).isEmpty();
        verify(paymentRepository).findByBuyerUserIdAndPaymentStatusOrderByCreatedAtDesc(
                buyerId,
                PaymentStatus.PAID);
    }

    @Test
    void handleStripeWebhook_success_activatesReviewerWhenMissing_TC005() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        mockPaymentSave();
        mockPaymentDetailSave();
        when(reviewerRepository.findByUserUserId(buyerId)).thenReturn(Optional.empty());
        mockReviewerSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "REVIEWER")).thenReturn(false);
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role("REVIEWER")));

        paymentService.handleStripeWebhook(stripePayload(), null);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(detail.getProviderTransactionId()).isEqualTo("pi_test_123");
        verify(reviewerRepository).save(any(Reviewer.class));
        verify(userRoleAssignmentRepository).save(any());
    }

    @Test
    void handleStripeWebhook_success_existingExpiredReviewerReused_TC006() {
        Reviewer existing = new Reviewer();
        existing.setReviewerId(UUID.randomUUID());
        existing.setUser(user());
        existing.setReviewerActive(false);
        existing.setReviewerExpiresAt(LocalDateTime.now().minusMonths(1));
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        mockPaymentSave();
        mockPaymentDetailSave();
        when(reviewerRepository.findByUserUserId(buyerId)).thenReturn(Optional.of(existing));
        mockReviewerSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "REVIEWER")).thenReturn(true);

        paymentService.handleStripeWebhook(stripePayload(), null);

        assertThat(existing.getReviewerActive()).isTrue();
        assertThat(existing.getReviewerExpiresAt()).isAfter(LocalDateTime.now().plusMonths(5));
        verify(reviewerRepository).save(existing);
        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    void handleStripeWebhook_success_existingActiveReviewerExtendsFromCurrentExpiry_TC020() {
        Reviewer existing = new Reviewer();
        existing.setReviewerId(UUID.randomUUID());
        existing.setUser(user());
        existing.setReviewerActive(true);
        LocalDateTime currentExpiry = LocalDateTime.now().plusMonths(6);
        existing.setReviewerExpiresAt(currentExpiry);
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        mockPaymentSave();
        mockPaymentDetailSave();
        when(reviewerRepository.findByUserUserId(buyerId)).thenReturn(Optional.of(existing));
        mockReviewerSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "REVIEWER")).thenReturn(true);

        paymentService.handleStripeWebhook(stripePayload(), null);

        assertThat(existing.getReviewerActive()).isTrue();
        assertThat(existing.getReviewerExpiresAt()).isAfter(currentExpiry.plusMonths(6).minusSeconds(1));
        assertThat(existing.getReviewerExpiresAt()).isBefore(currentExpiry.plusMonths(6).plusSeconds(1));
        verify(reviewerRepository).save(existing);
        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    void markBankTransferPaid_success_activatesReviewer_TC007() {
        Payment payment = pendingPayment(PaymentMethod.BANK_TRANSFER,
                extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        mockPaymentSave();
        when(reviewerRepository.findByUserUserId(buyerId)).thenReturn(Optional.empty());
        mockReviewerSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "ADMIN")).thenReturn(true);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "REVIEWER")).thenReturn(false);
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role("REVIEWER")));

        PaymentResponseDTO result = paymentService.markBankTransferPaid(buyerId, paymentId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getPaidAt()).isNotNull();
        verify(reviewerRepository).save(any(Reviewer.class));
        verify(userRoleAssignmentRepository).save(any());
    }

    @Test
    void markBankTransferPaid_success_cafePageOpeningCreatesDraftCafePage_TC026() {
        Payment payment = pendingPayment(PaymentMethod.BANK_TRANSFER, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.getExtraFee().setMaxMembers(5);
        PaymentDetail detail = paymentDetail(payment);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        mockPaymentSave();
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of());
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "ADMIN")).thenReturn(true);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(false);
        when(roleRepository.findByName("CAFE_PAGE")).thenReturn(Optional.of(role("CAFE_PAGE")));

        PaymentResponseDTO result = paymentService.markBankTransferPaid(buyerId, paymentId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(cafePageRepository).save(org.mockito.ArgumentMatchers
                .argThat(page -> page.getMaxMembers() == 5 && Boolean.TRUE.equals(page.getPageActive())));
        verify(pageMemberRepository).save(any(PageMember.class));
        verify(userRoleAssignmentRepository).save(any());
    }

    @Test
    void markBankTransferPaid_success_duplicateAlreadyPaidDoesNotCreateDuplicateCafePage_TC027() {
        Payment payment = pendingPayment(PaymentMethod.BANK_TRANSFER, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.setPaymentStatus(PaymentStatus.PAID);
        PaymentDetail detail = paymentDetail(payment);
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "ADMIN")).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));

        PaymentResponseDTO result = paymentService.markBankTransferPaid(buyerId, paymentId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(cafePageRepository, never()).save(any());
        verify(pageMemberRepository, never()).save(any());
        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    void handleStripeWebhook_success_duplicateAlreadyPaidIsIdempotent_TC008() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        payment.setPaymentStatus(PaymentStatus.PAID);
        PaymentDetail detail = paymentDetail(payment);
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.of(detail));
        mockPaymentDetailSave();

        paymentService.handleStripeWebhook(stripePayload(), null);

        verify(reviewerRepository, never()).save(any());
        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    void handleStripeWebhook_fail_invalidSignatureRejectedByService_TC030() {
        PaymentServiceImpl serviceWithWebhookSecret = new PaymentServiceImpl(
                paymentRepository,
                paymentDetailRepository,
                adFeeRepository,
                extraFeeRepository,
                userRepository,
                reviewerRepository,
                cafePageRepository,
                pageMemberRepository,
                roleRepository,
                userRoleAssignmentRepository,
                stripeCheckoutClient,
                vnpayPaymentClient,
                new ObjectMapper(),
                cafePageValidator,
                "whsec_test_secret");

        assertThatThrownBy(() -> serviceWithWebhookSecret.handleStripeWebhook(stripePayload(), "invalid-signature"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Invalid Stripe signature");
                });

        verify(paymentDetailRepository, never()).findByProviderOrderId(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleVnpayIpn_success_updatesPayment_TC013() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        PaymentDetail detail = paymentDetail(payment);
        detail.setProviderName("VNPAY");
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of());
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(false);
        when(roleRepository.findByName("CAFE_PAGE")).thenReturn(Optional.of(role("CAFE_PAGE")));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("00");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(detail.getProviderTransactionId()).isEqualTo("14123456");
        verify(paymentRepository, times(2)).save(payment);
        verify(paymentDetailRepository).save(detail);
        verify(cafePageRepository).save(any(CafePage.class));
        verify(pageMemberRepository).save(any(PageMember.class));
        verify(userRoleAssignmentRepository).save(any());
    }

    @Test
    void handleVnpayIpn_success_duplicateAlreadyPaidIsIdempotent_TC014() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.setPaymentStatus(PaymentStatus.PAID);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("02");
        assertThat(result.getMessage()).isEqualTo("Order already confirmed");
        verify(paymentRepository, never()).save(any());
        verify(paymentDetailRepository, never()).save(any());
        verify(cafePageRepository, never()).save(any());
        verify(pageMemberRepository, never()).save(any());
    }

    @Test
    void handleVnpayIpn_success_cafePageOpeningCreatesDraftCafePageForBuyer_TC021() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.getExtraFee().setMaxMembers(5);
        payment.getBuyer().setUserFullName("Nguyen Van A");
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of());
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(false);
        when(roleRepository.findByName("CAFE_PAGE")).thenReturn(Optional.of(role("CAFE_PAGE")));

        paymentService.handleVnpayIpn(params);

        verify(cafePageRepository)
                .save(org.mockito.ArgumentMatchers.argThat(page -> page.getOwner().equals(payment.getBuyer())
                        && page.getName().equals("Nguyen Van A's Cafe Page")
                        && page.getAddress().equals("Pending update")
                        && page.getStatus() == PageStatus.DRAFT
                        && page.getLikeCount() == 0
                        && page.getFollowerCount() == 0
                        && page.getMaxMembers() == 5
                        && Boolean.TRUE.equals(page.getPageActive())));
    }

    @Test
    void handleVnpayIpn_success_cafePageOpeningDefaultsCafePageMaxMembers_TC028() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.getExtraFee().setMaxMembers(null);
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of());
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(true);

        paymentService.handleVnpayIpn(params);

        verify(cafePageRepository).save(org.mockito.ArgumentMatchers.argThat(page -> page.getMaxMembers() == 2));
    }

    @Test
    void handleVnpayIpn_success_existingCafePageReusedNoDuplicate_TC022() {
        CafePage existingPage = cafePage(user());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.getExtraFee().setMaxMembers(6);
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of(existingPage));
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(existingPage.getId(), buyerId))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(true);

        paymentService.handleVnpayIpn(params);

        assertThat(existingPage.getMaxMembers()).isEqualTo(6);
        verify(cafePageRepository).save(existingPage);
        verify(pageMemberRepository).save(any(PageMember.class));
    }

    @Test
    void handleVnpayIpn_success_existingCafePageMaxMembersUpdatedFromPackage_TC029() {
        CafePage existingPage = cafePage(user());
        existingPage.setMaxMembers(2);
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        payment.getExtraFee().setMaxMembers(8);
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of(existingPage));
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(existingPage.getId(), buyerId))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(true);

        paymentService.handleVnpayIpn(params);

        assertThat(existingPage.getMaxMembers()).isEqualTo(8);
        verify(cafePageRepository).save(existingPage);
    }

    @Test
    void handleVnpayIpn_success_ownerPageMemberIsCreated_TC023() {
        CafePage existingPage = cafePage(user());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of(existingPage));
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(existingPage.getId(), buyerId))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(true);

        paymentService.handleVnpayIpn(params);

        verify(pageMemberRepository)
                .save(org.mockito.ArgumentMatchers.argThat(member -> member.getCafePage().equals(existingPage)
                        && member.getUser().getUserId().equals(buyerId)
                        && PageMember.ROLE_OWNER.equals(member.getRoleName())
                        && member.getStatus() == PageMemberStatus.ACTIVE));
    }

    @Test
    void handleVnpayIpn_success_cafePageRoleAssignedIfMissing_TC024() {
        CafePage existingPage = cafePage(user());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(cafePageRepository.findByOwnerUserId(buyerId)).thenReturn(List.of(existingPage));
        mockCafePageSave();
        when(pageMemberRepository.findByCafePageIdAndUserUserId(existingPage.getId(), buyerId))
                .thenReturn(Optional.empty());
        mockPageMemberSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "CAFE_PAGE")).thenReturn(false);
        when(roleRepository.findByName("CAFE_PAGE")).thenReturn(Optional.of(role("CAFE_PAGE")));

        paymentService.handleVnpayIpn(params);

        verify(userRoleAssignmentRepository).save(any());
    }

    @Test
    void handleVnpayIpn_fail_invalidSignature_TC015() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(false);

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("97");
        verify(paymentRepository, never()).findById(any());
    }

    @Test
    void handleVnpayIpn_fail_amountMismatch_TC016() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        Map<String, String> params = vnpayParams("00", "00", "10000000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("04");
        assertThat(result.getMessage()).isEqualTo("invalid amount");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleVnpayIpn_fail_orderIdMismatch_TC017() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", UUID.randomUUID().toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("01");
        assertThat(result.getMessage()).isEqualTo("Order not found");
    }

    @Test
    void handleVnpayIpn_failedPayment_updatesFailed_TC018() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("51", "02", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("00");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(detail.getFailureCode()).isEqualTo("51");
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleVnpayIpn_cancelledPayment_updatesCancelled_TC019() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        PaymentDetail detail = paymentDetail(payment);
        Map<String, String> params = vnpayParams("24", "02", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("00");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(detail.getFailureCode()).isEqualTo("24");
    }

    @Test
    void stripeCheckoutClient_fail_missingOrInvalidSecretKey_TC009() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));

        assertThatThrownBy(() -> new StripeCheckoutClientImpl("", "http://localhost/success", "http://localhost/cancel")
                .createCheckoutSession(payment))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Stripe secret key is not configured");
                });

        assertThatThrownBy(() -> new StripeCheckoutClientImpl("not-a-secret", "http://localhost/success",
                "http://localhost/cancel")
                .createCheckoutSession(payment))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Stripe secret key format is invalid");
                });
    }

    @Test
    void stripeCheckoutClient_fail_blankSuccessOrCancelUrl_TC010() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));

        assertThatThrownBy(() -> new StripeCheckoutClientImpl("sk_test_123", "", "http://localhost/cancel")
                .createCheckoutSession(payment))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Stripe success URL is not configured");
                });

        assertThatThrownBy(() -> new StripeCheckoutClientImpl("sk_test_123", "http://localhost/success", "")
                .createCheckoutSession(payment))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Stripe cancel URL is not configured");
                });
    }

    @Test
    void stripeCheckoutClient_fail_stripeExceptionMappedToSafe502Message_TC011() throws Exception {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        StripeCheckoutClientImpl client = new StripeCheckoutClientImpl(
                "sk_test_123",
                "http://localhost/success",
                "http://localhost/cancel");

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new ApiException("No such payment method type", "req_123", "invalid_request_error", 400,
                            null));

            assertThatThrownBy(() -> client.createCheckoutSession(payment))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(error -> {
                        ResponseStatusException ex = (ResponseStatusException) error;
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                        assertThat(ex.getReason())
                                .isEqualTo("Stripe checkout session creation failed: No such payment method type");
                    });
        }
    }

    // ------------------------------------------------------- getPayment

    @Test
    void getPayment_success_buyerReadsOwnPayment_TC035() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId))
                .thenReturn(Optional.of(paymentDetail(payment)));

        PaymentResponseDTO result = paymentService.getPayment(buyerId, paymentId);

        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getPaymentUrl()).isEqualTo("https://checkout.stripe.com/test");
        assertThat(result.getExtraFeeType()).isEqualTo(ExtraFeeType.REVIEWER_REGISTRATION);
    }

    @Test
    void getPayment_success_adminReadsSomeoneElsePayment_TC036() {
        UUID adminId = UUID.randomUUID();
        Payment payment = pendingPayment(PaymentMethod.BANK_TRANSFER, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(adminId, "ADMIN")).thenReturn(true);
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThat(paymentService.getPayment(adminId, paymentId).getPaymentUrl()).isNull();
    }

    @Test
    void getPayment_fail_strangerWithoutAdminRole_TC037() {
        UUID strangerId = UUID.randomUUID();
        Payment payment = pendingPayment(PaymentMethod.BANK_TRANSFER, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(strangerId, "ADMIN")).thenReturn(false);

        assertThatThrownBy(() -> paymentService.getPayment(strangerId, paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin role is required");
    }

    @Test
    void getPayment_fail_paymentNotFound_TC038() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(buyerId, paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void getMyPayments_fail_anonymousRequester_TC039() {
        assertThatThrownBy(() -> paymentService.getMyPayments(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Sign in required");
    }

    @Test
    void getAllPayments_fail_requesterIsNotAdmin_TC040() {
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "ADMIN")).thenReturn(false);

        assertThatThrownBy(() -> paymentService.getAllPayments(buyerId, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin role is required");
    }

    // --------------------------------------------- resolveTargetCafePage

    @Test
    void createPayment_success_cafePageIdBindsExistingPage_TC041() {
        User buyer = user();
        ExtraFee extraFee = extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING);
        CafePage cafePage = cafePage(buyer);
        CreatePaymentRequestDTO request = request(PaymentMethod.BANK_TRANSFER);
        request.setCafePageId(cafePage.getId());

        mockPaymentSave();
        mockPaymentDetailSave();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        when(cafePageValidator.validateCafePageExists(cafePage.getId())).thenReturn(cafePage);

        PaymentResponseDTO result = paymentService.createPayment(buyerId, request);

        assertThat(result.getActivatedCafePageId()).isEqualTo(cafePage.getId());
        verify(cafePageValidator).validateUserCanManagePage(cafePage.getId(), buyerId);
    }

    @Test
    void createPayment_fail_cafePageIdOnNonCafePagePackage_TC042() {
        User buyer = user();
        ExtraFee extraFee = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        CreatePaymentRequestDTO request = request(PaymentMethod.BANK_TRANSFER);
        request.setCafePageId(UUID.randomUUID());

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cafePageId is only supported for cafe page packages");
    }

    @Test
    void createPayment_fail_cafePageIdOnAdFeePackage_TC043() {
        User buyer = user();
        AdFee adFee = adFee(true);
        CreatePaymentRequestDTO request = adFeeRequest(PaymentMethod.BANK_TRANSFER);
        request.setCafePageId(UUID.randomUUID());

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(adFeeRepository.findById(adFeeId)).thenReturn(Optional.of(adFee));

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cafePageId is only supported for cafe page packages");
    }

    @Test
    void createPayment_success_adFeeWithBlankCurrencyFallsBackToVnd_TC044() {
        User buyer = user();
        AdFee adFee = adFee(true);
        adFee.setCurrency("  ");

        mockPaymentSave();
        mockPaymentDetailSave();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(adFeeRepository.findById(adFeeId)).thenReturn(Optional.of(adFee));

        assertThat(paymentService.createPayment(buyerId, adFeeRequest(PaymentMethod.BANK_TRANSFER)).getCurrency())
                .isEqualTo("VND");
    }

    @Test
    void createPayment_fail_paymentMethodIsNull_TC045() {
        User buyer = user();
        ExtraFee extraFee = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        CreatePaymentRequestDTO request = request(null);

        mockPaymentSave();
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));

        assertThatThrownBy(() -> paymentService.createPayment(buyerId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid payment method");
    }

    // ------------------------------------------------- handleVnpayReturn

    @Test
    void handleVnpayReturn_fail_invalidSignature_TC046() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(false);

        var result = paymentService.handleVnpayReturn(params);

        assertThat(result.getStatus()).isEqualTo("failed");
        assertThat(result.getMessage()).isEqualTo("Invalid signature");
        assertThat(result.getPaymentId()).isNull();
    }

    @Test
    void handleVnpayReturn_fail_unparsableTxnRef_TC047() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", "khong-phai-uuid");
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);

        assertThat(paymentService.handleVnpayReturn(params).getMessage()).isEqualTo("Payment not found");
    }

    @Test
    void handleVnpayReturn_fail_paymentMissingOrWrongMethod_TC048() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        Payment stripePayment = pendingPayment(
                PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(stripePayment));

        assertThat(paymentService.handleVnpayReturn(params).getMessage()).isEqualTo("Payment not found");
        assertThat(paymentService.handleVnpayReturn(params).getMessage()).isEqualTo("Payment not found");
    }

    @Test
    void handleVnpayReturn_fail_amountMismatch_TC049() {
        Map<String, String> params = vnpayParams("00", "00", "10000", paymentId.toString());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        var result = paymentService.handleVnpayReturn(params);

        assertThat(result.getStatus()).isEqualTo("failed");
        assertThat(result.getMessage()).isEqualTo("Invalid amount");
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void handleVnpayReturn_fail_amountUnparsableOrMissing_TC050() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        Map<String, String> blankAmount = vnpayParams("00", "00", "  ", paymentId.toString());
        Map<String, String> textAmount = vnpayParams("00", "00", "abc", paymentId.toString());
        Map<String, String> fractionalAmount = vnpayParams("00", "00", "0.5", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(any())).thenReturn(true);

        assertThat(paymentService.handleVnpayReturn(blankAmount).getMessage()).isEqualTo("Invalid amount");
        assertThat(paymentService.handleVnpayReturn(textAmount).getMessage()).isEqualTo("Invalid amount");
        assertThat(paymentService.handleVnpayReturn(fractionalAmount).getMessage()).isEqualTo("Invalid amount");
    }

    @Test
    void handleVnpayReturn_success_transactionSucceeded_TC051() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        var result = paymentService.handleVnpayReturn(params);

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getTransactionNo()).isEqualTo("14123456");
    }

    @Test
    void handleVnpayReturn_success_alreadyPaidIsReportedAsSuccess_TC052() {
        Map<String, String> params = vnpayParams("99", "99", "29900000", paymentId.toString());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        payment.setPaymentStatus(PaymentStatus.PAID);
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThat(paymentService.handleVnpayReturn(params).getStatus()).isEqualTo("success");
    }

    @Test
    void handleVnpayReturn_success_pendingTransactionStaysPending_TC053() {
        Map<String, String> params = vnpayParams("00", "01", "29900000", paymentId.toString());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        var result = paymentService.handleVnpayReturn(params);

        assertThat(result.getStatus()).isEqualTo("pending");
        assertThat(result.getMessage()).isEqualTo("Payment pending");
    }

    @Test
    void handleVnpayReturn_fail_declinedTransaction_TC054() {
        Map<String, String> params = vnpayParams("24", "02", "29900000", paymentId.toString());
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        var result = paymentService.handleVnpayReturn(params);

        assertThat(result.getStatus()).isEqualTo("failed");
        assertThat(result.getMessage()).isEqualTo("Payment failed");
        assertThat(result.getResponseCode()).isEqualTo("24");
    }

    @Test
    void handleVnpayIpn_fail_unexpectedExceptionBecomesCode99_TC055() {
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenThrow(new IllegalStateException("vnpay down"));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("99");
        assertThat(result.getMessage()).isEqualTo("Unknown error");
    }

    // ------------------------------------------------ syncStripePayment

    @Test
    void syncStripePayment_success_completeSessionMarksPaid_TC056() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        mockPaymentSave();
        mockPaymentDetailSave();
        mockReviewerSave();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(stripeCheckoutClient.getSessionStatus("cs_test_123")).thenReturn("complete");
        when(stripeCheckoutClient.getPaymentIntentId("cs_test_123")).thenReturn("pi_test_123");
        when(reviewerRepository.findByUserUserId(buyerId)).thenReturn(Optional.empty());
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "REVIEWER")).thenReturn(true);

        PaymentResponseDTO result = paymentService.syncStripePayment(buyerId, paymentId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(detail.getProviderTransactionId()).isEqualTo("pi_test_123");
    }

    @Test
    void syncStripePayment_success_expiredSessionMarksExpired_TC057() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        mockPaymentSave();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(stripeCheckoutClient.getSessionStatus("cs_test_123")).thenReturn("expired");

        assertThat(paymentService.syncStripePayment(buyerId, paymentId).getPaymentStatus())
                .isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    void syncStripePayment_success_openSessionLeavesStatusUntouched_TC058() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        when(stripeCheckoutClient.getSessionStatus("cs_test_123")).thenReturn("open");

        assertThat(paymentService.syncStripePayment(buyerId, paymentId).getPaymentStatus())
                .isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void syncStripePayment_success_alreadySettledPaymentIsReturnedAsIs_TC059() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        payment.setPaymentStatus(PaymentStatus.PAID);
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThat(paymentService.syncStripePayment(buyerId, paymentId).getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);
        verify(stripeCheckoutClient, never()).getSessionStatus(any());
    }

    @Test
    void syncStripePayment_fail_paymentNotFound_TC060() {
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.syncStripePayment(buyerId, paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void syncStripePayment_fail_notAStripePayment_TC061() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.syncStripePayment(buyerId, paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Sync is only supported for Stripe payments");
    }

    @Test
    void syncStripePayment_fail_sessionAlreadyExpired_TC062() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        payment.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.syncStripePayment(buyerId, paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.GONE));
    }

    @Test
    void syncStripePayment_fail_noStripeSessionRecorded_TC063() {
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.syncStripePayment(buyerId, paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    // ------------------------------------- activation failure -> refund

    @Test
    void markPaymentPaid_fail_activationErrorTriggersStripeRefund_TC064() {
        // Gói reviewer thiếu durationMonths làm bước kích hoạt ném lỗi: tiền đã
        // thu nên phải hoàn lại và đưa payment về REFUNDED.
        ExtraFee brokenPackage = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        brokenPackage.setDurationMonths(null);
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, brokenPackage);
        PaymentDetail detail = paymentDetail(payment);
        mockPaymentSave();
        mockPaymentDetailSave();
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));

        paymentService.handleStripeWebhook(stripePayload(), null);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(stripeCheckoutClient).refundPaymentIntent("pi_test_123");
    }

    @Test
    void markPaymentPaid_fail_refundFailureIsSwallowed_TC065() {
        ExtraFee brokenPackage = extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION);
        brokenPackage.setDurationMonths(null);
        Payment payment = pendingPayment(PaymentMethod.STRIPE_CARD, brokenPackage);
        PaymentDetail detail = paymentDetail(payment);
        mockPaymentSave();
        mockPaymentDetailSave();
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.of(detail));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        org.mockito.Mockito.doThrow(new IllegalStateException("stripe down"))
                .when(stripeCheckoutClient).refundPaymentIntent("pi_test_123");

        paymentService.handleStripeWebhook(stripePayload(), null);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void handleStripeWebhook_success_ignoresOtherEventTypes_TC066() {
        String payload = """
                {"type":"payment_intent.created","data":{"object":{"id":"cs_x"}}}
                """;

        paymentService.handleStripeWebhook(payload, null);

        verify(paymentDetailRepository, never()).findByProviderOrderId(any());
    }

    @Test
    void handleStripeWebhook_fail_malformedPayload_TC067() {
        assertThatThrownBy(() -> paymentService.handleStripeWebhook("khong-phai-json", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid Stripe webhook payload");
    }

    @Test
    void handleStripeWebhook_fail_sessionNotFound_TC068() {
        when(paymentDetailRepository.findByProviderOrderId("cs_test_123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handleStripeWebhook(stripePayload(), null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment detail not found");
    }

    private CreatePaymentRequestDTO request(PaymentMethod method) {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setExtraFeeId(extraFeeId);
        request.setPaymentMethod(method);
        return request;
    }

    private CreatePaymentRequestDTO adFeeRequest(PaymentMethod method) {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setAdFeeId(adFeeId);
        request.setPaymentMethod(method);
        return request;
    }

    private User user() {
        User user = new User();
        user.setUserId(buyerId);
        user.setUserName("buyer");
        user.setUserEmail("buyer@example.com");
        user.setUserPassword("secret");
        user.setAccountStatus(true);
        return user;
    }

    private ExtraFee extraFee(boolean active, ExtraFeeType type) {
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(extraFeeId);
        extraFee.setName("Reviewer 6 month package");
        extraFee.setFeeType(type);
        extraFee.setPrice(299000);
        extraFee.setDurationMonths(6);
        extraFee.setStatus(active);
        return extraFee;
    }

    private AdFee adFee(boolean active) {
        AdFee adFee = new AdFee();
        adFee.setAdFeeId(adFeeId);
        adFee.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        adFee.setPrice(BigDecimal.valueOf(500000));
        adFee.setCurrency("VND");
        adFee.setStatus(active);
        return adFee;
    }

    private Payment pendingPayment(PaymentMethod method, ExtraFee extraFee) {
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBuyer(user());
        payment.setExtraFee(extraFee);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(BigDecimal.valueOf(extraFee.getPrice()));
        payment.setCurrency("VND");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(30));
        return payment;
    }

    private PaymentDetail paymentDetail(Payment payment) {
        PaymentDetail detail = new PaymentDetail();
        detail.setPaymentDetailId(UUID.randomUUID());
        detail.setPayment(payment);
        detail.setProviderName("STRIPE");
        detail.setProviderOrderId("cs_test_123");
        detail.setProviderPaymentUrl("https://checkout.stripe.com/test");
        return detail;
    }

    private CafePage cafePage(User owner) {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setOwner(owner);
        cafePage.setName("Existing Cafe Page");
        cafePage.setAddress("Existing address");
        cafePage.setStatus(PageStatus.DRAFT);
        cafePage.setLikeCount(0);
        cafePage.setFollowerCount(0);
        return cafePage;
    }

    private Role role(String roleName) {
        Role role = new Role();
        role.setId(2);
        role.setName(roleName);
        return role;
    }

    private String stripePayload() {
        return """
                {
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_123",
                      "payment_intent": "pi_test_123"
                    }
                  }
                }
                """;
    }

    private Map<String, String> vnpayParams(String responseCode, String transactionStatus, String amount,
            String txnRef) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Amount", amount);
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TransactionStatus", transactionStatus);
        params.put("vnp_TransactionNo", "14123456");
        params.put("vnp_SecureHash", "valid-signature");
        return params;
    }

    private void mockPaymentSave() {
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getPaymentId() == null) {
                payment.setPaymentId(paymentId);
            }
            if (payment.getCreatedAt() == null) {
                payment.setCreatedAt(LocalDateTime.now());
            }
            return payment;
        }).when(paymentRepository).save(any(Payment.class));
    }

    private void mockPaymentDetailSave() {
        doAnswer(invocation -> {
            PaymentDetail detail = invocation.getArgument(0);
            if (detail.getPaymentDetailId() == null) {
                detail.setPaymentDetailId(UUID.randomUUID());
            }
            return detail;
        }).when(paymentDetailRepository).save(any(PaymentDetail.class));
    }

    private void mockReviewerSave() {
        doAnswer(invocation -> {
            Reviewer reviewer = invocation.getArgument(0);
            if (reviewer.getReviewerId() == null) {
                reviewer.setReviewerId(UUID.randomUUID());
            }
            return reviewer;
        }).when(reviewerRepository).save(any(Reviewer.class));
    }

    private void mockCafePageSave() {
        doAnswer(invocation -> {
            CafePage cafePage = invocation.getArgument(0);
            if (cafePage.getId() == null) {
                cafePage.setId(UUID.randomUUID());
            }
            return cafePage;
        }).when(cafePageRepository).save(any(CafePage.class));
    }

    private void mockPageMemberSave() {
        doAnswer(invocation -> invocation.getArgument(0))
                .when(pageMemberRepository).save(any(PageMember.class));
    }
}
