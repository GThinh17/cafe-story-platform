package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
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
import com.cafestory.service.serviceInterface.PaymentService;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String REVIEWER_ROLE = "REVIEWER";

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final ExtraFeeRepository extraFeeRepository;
    private final UserRepository userRepository;
    private final ReviewerRepository reviewerRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final StripeCheckoutClient stripeCheckoutClient;
    private final ObjectMapper objectMapper;
    private final String stripeWebhookSecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentDetailRepository paymentDetailRepository,
            ExtraFeeRepository extraFeeRepository,
            UserRepository userRepository,
            ReviewerRepository reviewerRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            StripeCheckoutClient stripeCheckoutClient,
            ObjectMapper objectMapper,
            @Value("${stripe.webhook-secret:}") String stripeWebhookSecret) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.extraFeeRepository = extraFeeRepository;
        this.userRepository = userRepository;
        this.reviewerRepository = reviewerRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.stripeCheckoutClient = stripeCheckoutClient;
        this.objectMapper = objectMapper;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(CreatePaymentRequestDTO request) {
        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));
        ExtraFee extraFee = extraFeeRepository.findById(request.getExtraFeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found"));
        if (!Boolean.TRUE.equals(extraFee.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extra fee is inactive");
        }

        Payment payment = new Payment();
        payment.setBuyer(buyer);
        payment.setExtraFee(extraFee);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(BigDecimal.valueOf(extraFee.getPrice()));
        payment.setCurrency("VND");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(30));
        Payment savedPayment = paymentRepository.save(payment);

        PaymentDetail detail = new PaymentDetail();
        detail.setPayment(savedPayment);
        if (request.getPaymentMethod() == PaymentMethod.STRIPE_CARD) {
            StripeCheckoutClient.StripeCheckoutSession session = stripeCheckoutClient.createCheckoutSession(savedPayment, extraFee);
            detail.setProviderName("STRIPE");
            detail.setProviderOrderId(session.sessionId());
            detail.setProviderPaymentUrl(session.paymentUrl());
            detail.setRawResponse(session.rawResponse());
        } else if (request.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            detail.setProviderName("BANK_TRANSFER");
            detail.setTransferContent("CAFE_PAYMENT_" + savedPayment.getPaymentId());
            detail.setNote("Manual bank transfer payment. Mark as paid after transfer is verified.");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method");
        }

        PaymentDetail savedDetail = paymentDetailRepository.save(detail);
        savedPayment.setPaymentDetail(savedDetail);
        return toResponse(savedPayment, savedDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(UUID paymentId) {
        Payment payment = validatePaymentExists(paymentId);
        return toResponse(payment, paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null));
    }

    @Override
    @Transactional
    public PaymentResponseDTO markBankTransferPaid(UUID paymentId) {
        Payment payment = validatePaymentExists(paymentId);
        if (payment.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is not a bank transfer");
        }
        markPaymentPaid(payment, null);
        PaymentDetail detail = paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null);
        return toResponse(payment, detail);
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        validateStripeSignatureIfConfigured(payload, signatureHeader);
        StripeWebhookData webhookData = parseStripeWebhookData(payload);
        if (!"checkout.session.completed".equals(webhookData.type())) {
            return;
        }
        PaymentDetail detail = paymentDetailRepository.findByProviderOrderId(webhookData.sessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment detail not found"));
        detail.setProviderTransactionId(webhookData.paymentIntentId());
        detail.setRawResponse(payload);
        paymentDetailRepository.save(detail);
        markPaymentPaid(detail.getPayment(), webhookData.paymentIntentId());
    }

    private Payment validatePaymentExists(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void markPaymentPaid(Payment payment, String providerTransactionId) {
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        if (providerTransactionId != null) {
            paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()).ifPresent(detail -> {
                detail.setProviderTransactionId(providerTransactionId);
                paymentDetailRepository.save(detail);
            });
        }
        activatePurchasedProduct(payment);
    }

    private void activatePurchasedProduct(Payment payment) {
        ExtraFee extraFee = payment.getExtraFee();
        if (extraFee.getFeeType() == ExtraFeeType.REVIEWER_REGISTRATION) {
            activateReviewerSubscription(payment.getBuyer(), extraFee);
        }
    }

    private void activateReviewerSubscription(User buyer, ExtraFee extraFee) {
        if (extraFee.getDurationMonths() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reviewer package durationMonths is required");
        }
        Reviewer reviewer = reviewerRepository.findByUserUserId(buyer.getUserId()).orElseGet(Reviewer::new);
        reviewer.setUser(buyer);
        reviewer.setReviewerActive(true);
        reviewer.setReviewerExpiresAt(LocalDateTime.now().plusMonths(extraFee.getDurationMonths()));
        reviewerRepository.save(reviewer);
        assignReviewerRole(buyer);
    }

    private void assignReviewerRole(User user) {
        if (userRoleAssignmentRepository.existsByUserUserIdAndRoleName(user.getUserId(), REVIEWER_ROLE)) {
            return;
        }
        Role role = roleRepository.findByName(REVIEWER_ROLE).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(REVIEWER_ROLE);
            return roleRepository.save(newRole);
        });
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        userRoleAssignmentRepository.save(assignment);
    }

    private void validateStripeSignatureIfConfigured(String payload, String signatureHeader) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            return;
        }
        try {
            Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        }
    }

    private StripeWebhookData parseStripeWebhookData(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText();
            JsonNode session = root.path("data").path("object");
            return new StripeWebhookData(
                    type,
                    session.path("id").asText(null),
                    session.path("payment_intent").asText(null));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook payload");
        }
    }

    private PaymentResponseDTO toResponse(Payment payment, PaymentDetail detail) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(payment.getPaymentId());
        response.setBuyerId(payment.getBuyer().getUserId());
        response.setExtraFeeId(payment.getExtraFee().getExtraFeeId());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setCreatedAt(payment.getCreatedAt());
        response.setPaidAt(payment.getPaidAt());
        response.setExpiredAt(payment.getExpiredAt());
        if (detail != null) {
            response.setPaymentUrl(detail.getProviderPaymentUrl());
            response.setQrCodeUrl(detail.getProviderQrCodeUrl());
            response.setTransferContent(detail.getTransferContent());
        }
        return response;
    }

    private record StripeWebhookData(String type, String sessionId, String paymentIntentId) {
    }
}
