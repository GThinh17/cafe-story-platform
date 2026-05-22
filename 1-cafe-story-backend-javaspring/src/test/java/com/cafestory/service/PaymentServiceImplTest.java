package com.cafestory.service;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.ExtraFeeRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private final UUID buyerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID extraFeeId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID paymentId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDetailRepository paymentDetailRepository;

    @Mock
    private ExtraFeeRepository extraFeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewerRepository reviewerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private StripeCheckoutClient stripeCheckoutClient;

    @Mock
    private VnpayPaymentClient vnpayPaymentClient;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                paymentDetailRepository,
                extraFeeRepository,
                userRepository,
                reviewerRepository,
                roleRepository,
                userRoleAssignmentRepository,
                stripeCheckoutClient,
                vnpayPaymentClient,
                new ObjectMapper(),
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
        when(stripeCheckoutClient.createCheckoutSession(any(Payment.class), any(ExtraFee.class)))
                .thenReturn(new StripeCheckoutClient.StripeCheckoutSession("cs_test_123", "https://checkout.stripe.com/test", "{}"));

        PaymentResponseDTO result = paymentService.createPayment(request(PaymentMethod.STRIPE_CARD));

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

        PaymentResponseDTO result = paymentService.createPayment(request(PaymentMethod.BANK_TRANSFER));

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
        when(vnpayPaymentClient.createPaymentUrl(any(Payment.class), any(ExtraFee.class)))
                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=" + paymentId);

        PaymentResponseDTO result = paymentService.createPayment(request(PaymentMethod.VNPAY));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.VNPAY);
        assertThat(result.getPaymentUrl()).contains("vnp_TxnRef=" + paymentId);
        assertThat(result.getTransferContent()).isEqualTo("CAFE_VNPAY_" + paymentId);
    }

    @Test
    void createPayment_fail_buyerNotFound_TC003() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(request(PaymentMethod.STRIPE_CARD)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createPayment_fail_extraFeeNotFoundOrInactive_TC004() {
        when(userRepository.findById(buyerId)).thenReturn(Optional.of(user()));
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(request(PaymentMethod.STRIPE_CARD)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee(false, ExtraFeeType.REVIEWER_REGISTRATION)));

        assertThatThrownBy(() -> paymentService.createPayment(request(PaymentMethod.STRIPE_CARD)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
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
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role()));

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
        Payment payment = pendingPayment(PaymentMethod.BANK_TRANSFER, extraFee(true, ExtraFeeType.REVIEWER_REGISTRATION));
        PaymentDetail detail = paymentDetail(payment);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));
        mockPaymentSave();
        when(reviewerRepository.findByUserUserId(buyerId)).thenReturn(Optional.empty());
        mockReviewerSave();
        when(userRoleAssignmentRepository.existsByUserUserIdAndRoleName(buyerId, "REVIEWER")).thenReturn(false);
        when(roleRepository.findByName("REVIEWER")).thenReturn(Optional.of(role()));

        PaymentResponseDTO result = paymentService.markBankTransferPaid(paymentId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getPaidAt()).isNotNull();
        verify(reviewerRepository).save(any(Reviewer.class));
        verify(userRoleAssignmentRepository).save(any());
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
    void handleVnpayIpn_success_updatesPayment_TC013() {
        Payment payment = pendingPayment(PaymentMethod.VNPAY, extraFee(true, ExtraFeeType.CAFE_PAGE_OPENING));
        PaymentDetail detail = paymentDetail(payment);
        detail.setProviderName("VNPAY");
        Map<String, String> params = vnpayParams("00", "00", "29900000", paymentId.toString());
        when(vnpayPaymentClient.verifySignature(params)).thenReturn(true);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentDetailRepository.findByPaymentPaymentId(paymentId)).thenReturn(Optional.of(detail));

        VnpayIpnResponseDTO result = paymentService.handleVnpayIpn(params);

        assertThat(result.getRspCode()).isEqualTo("00");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(detail.getProviderTransactionId()).isEqualTo("14123456");
        verify(paymentRepository).save(payment);
        verify(paymentDetailRepository).save(detail);
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
                .createCheckoutSession(payment, payment.getExtraFee()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Stripe secret key is not configured");
                });

        assertThatThrownBy(() -> new StripeCheckoutClientImpl("not-a-secret", "http://localhost/success", "http://localhost/cancel")
                .createCheckoutSession(payment, payment.getExtraFee()))
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
                .createCheckoutSession(payment, payment.getExtraFee()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).isEqualTo("Stripe success URL is not configured");
                });

        assertThatThrownBy(() -> new StripeCheckoutClientImpl("sk_test_123", "http://localhost/success", "")
                .createCheckoutSession(payment, payment.getExtraFee()))
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
                    .thenThrow(new ApiException("No such payment method type", "req_123", "invalid_request_error", 400, null));

            assertThatThrownBy(() -> client.createCheckoutSession(payment, payment.getExtraFee()))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(error -> {
                        ResponseStatusException ex = (ResponseStatusException) error;
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                        assertThat(ex.getReason()).isEqualTo("Stripe checkout session creation failed: No such payment method type");
                    });
        }
    }

    private CreatePaymentRequestDTO request(PaymentMethod method) {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setBuyerId(buyerId);
        request.setExtraFeeId(extraFeeId);
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

    private Role role() {
        Role role = new Role();
        role.setId(2);
        role.setName("REVIEWER");
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

    private Map<String, String> vnpayParams(String responseCode, String transactionStatus, String amount, String txnRef) {
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
}
