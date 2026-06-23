package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.dto.responseDTO.VnpayReturnResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.PageMemberId;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
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
import com.cafestory.service.serviceInterface.PaymentService;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import com.cafestory.service.serviceInterface.VnpayPaymentClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final String REVIEWER_ROLE = "REVIEWER";
    private static final String CAFE_PAGE_ROLE = "CAFE_PAGE";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_CAFE_PAGE_ADDRESS = "Pending update";
    private static final int DEFAULT_CAFE_PAGE_MAX_MEMBERS = 2;

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final AdFeeRepository adFeeRepository;
    private final ExtraFeeRepository extraFeeRepository;
    private final UserRepository userRepository;
    private final ReviewerRepository reviewerRepository;
    private final CafePageRepository cafePageRepository;
    private final PageMemberRepository pageMemberRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final StripeCheckoutClient stripeCheckoutClient;
    private final VnpayPaymentClient vnpayPaymentClient;
    private final ObjectMapper objectMapper;
    private final String stripeWebhookSecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentDetailRepository paymentDetailRepository,
            AdFeeRepository adFeeRepository,
            ExtraFeeRepository extraFeeRepository,
            UserRepository userRepository,
            ReviewerRepository reviewerRepository,
            CafePageRepository cafePageRepository,
            PageMemberRepository pageMemberRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            StripeCheckoutClient stripeCheckoutClient,
            VnpayPaymentClient vnpayPaymentClient,
            ObjectMapper objectMapper,
            @Value("${stripe.webhook-secret:}") String stripeWebhookSecret) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.adFeeRepository = adFeeRepository;
        this.extraFeeRepository = extraFeeRepository;
        this.userRepository = userRepository;
        this.reviewerRepository = reviewerRepository;
        this.cafePageRepository = cafePageRepository;
        this.pageMemberRepository = pageMemberRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.stripeCheckoutClient = stripeCheckoutClient;
        this.vnpayPaymentClient = vnpayPaymentClient;
        this.objectMapper = objectMapper;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(UUID buyerId, CreatePaymentRequestDTO request) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));
        ProductPurchase productPurchase = resolveProductPurchase(request);

        Payment payment = new Payment();
        payment.setBuyer(buyer);
        payment.setExtraFee(productPurchase.extraFee());
        payment.setAdFee(productPurchase.adFee());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(productPurchase.amount());
        payment.setCurrency(productPurchase.currency());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        Payment savedPayment = paymentRepository.save(payment);

        PaymentDetail detail = new PaymentDetail();
        detail.setPayment(savedPayment);
        if (request.getPaymentMethod() == PaymentMethod.STRIPE_CARD) {
            StripeCheckoutClient.StripeCheckoutSession session = stripeCheckoutClient
                    .createCheckoutSession(savedPayment);
            detail.setProviderName("STRIPE");
            detail.setProviderOrderId(session.sessionId());
            detail.setProviderPaymentUrl(session.paymentUrl());
            detail.setRawResponse(session.rawResponse());
        } else if (request.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            detail.setProviderName("BANK_TRANSFER");
            detail.setTransferContent("CAFE_PAYMENT_" + savedPayment.getPaymentId());
            detail.setNote("Manual bank transfer payment. Mark as paid after transfer is verified.");
        } else if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            String paymentUrl = vnpayPaymentClient.createPaymentUrl(savedPayment);
            detail.setProviderName("VNPAY");
            detail.setProviderOrderId(savedPayment.getPaymentId().toString());
            detail.setProviderPaymentUrl(paymentUrl);
            detail.setTransferContent("CAFE_VNPAY_" + savedPayment.getPaymentId());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method");
        }

        PaymentDetail savedDetail = paymentDetailRepository.save(detail);
        savedPayment.setPaymentDetail(savedDetail);
        return toResponse(savedPayment, savedDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(UUID requesterUserId, UUID paymentId) {
        Payment payment = validatePaymentExists(paymentId);
        validatePaymentAccess(requesterUserId, payment);
        return toResponse(payment, paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments(UUID requesterUserId, PaymentStatus paymentStatus) {
        validateAdmin(requesterUserId);
        List<Payment> payments = paymentStatus == null
                ? paymentRepository.findAllByOrderByCreatedAtDesc()
                : paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(paymentStatus);
        return payments.stream()
                .map(payment -> toResponse(payment, paymentDetailRepository
                        .findByPaymentPaymentId(payment.getPaymentId())
                        .orElse(null)))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true)
    public PaymentResponseDTO markBankTransferPaid(UUID requesterUserId, UUID paymentId) {
        validateAdmin(requesterUserId);
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
    @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true)
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

    @Override
    @Transactional(readOnly = true)
    public VnpayReturnResponseDTO handleVnpayReturn(Map<String, String> params) {
        if (!vnpayPaymentClient.verifySignature(params)) {
            return vnpayReturn(null, "failed", null, params, "Invalid signature");
        }
        UUID paymentId = parsePaymentId(params.get("vnp_TxnRef")).orElse(null);
        if (paymentId == null) {
            return vnpayReturn(null, "failed", null, params, "Payment not found");
        }
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null || payment.getPaymentMethod() != PaymentMethod.VNPAY) {
            return vnpayReturn(paymentId, "failed", null, params, "Payment not found");
        }
        if (!amountMatches(payment, params.get("vnp_Amount"))) {
            return vnpayReturn(paymentId, "failed", payment, params, "Invalid amount");
        }
        if (payment.getPaymentStatus() == PaymentStatus.PAID || isVnpaySuccess(params)) {
            return vnpayReturn(paymentId, "success", payment, params, "Payment successful");
        }
        if (payment.getPaymentStatus() == PaymentStatus.PENDING && isVnpayPending(params)) {
            return vnpayReturn(paymentId, "pending", payment, params, "Payment pending");
        }
        return vnpayReturn(paymentId, "failed", payment, params, "Payment failed");
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true)
    public VnpayIpnResponseDTO handleVnpayIpn(Map<String, String> params) {
        try {
            if (!vnpayPaymentClient.verifySignature(params)) {
                return new VnpayIpnResponseDTO("97", "Invalid signature");
            }
            Optional<UUID> paymentId = parsePaymentId(params.get("vnp_TxnRef"));
            if (paymentId.isEmpty()) {
                return new VnpayIpnResponseDTO("01", "Order not found");
            }
            Payment payment = paymentRepository.findById(paymentId.get()).orElse(null);
            if (payment == null || payment.getPaymentMethod() != PaymentMethod.VNPAY) {
                return new VnpayIpnResponseDTO("01", "Order not found");
            }
            if (!amountMatches(payment, params.get("vnp_Amount"))) {
                return new VnpayIpnResponseDTO("04", "invalid amount");
            }
            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                return new VnpayIpnResponseDTO("02", "Order already confirmed");
            }

            PaymentDetail detail = paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()).orElse(null);
            updateVnpayDetail(detail, params);
            if (isVnpaySuccess(params)) {
                markPaymentPaid(payment, null);
            } else {
                markPaymentFailed(payment, params);
            }
            return new VnpayIpnResponseDTO("00", "Confirm Success");
        } catch (Exception ex) {
            return new VnpayIpnResponseDTO("99", "Unknown error");
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true)
    public PaymentResponseDTO syncStripePayment(UUID requesterUserId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        validatePaymentAccess(requesterUserId, payment);

        if (payment.getPaymentMethod() != PaymentMethod.STRIPE_CARD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sync is only supported for Stripe payments");
        }
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return toResponse(payment, paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null));
        }
        if (payment.getExpiredAt() != null && LocalDateTime.now().isAfter(payment.getExpiredAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Payment session expired. Cannot sync after expiry.");
        }

        PaymentDetail detail = paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null);
        String sessionId = detail != null ? detail.getProviderOrderId() : null;
        if (sessionId == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No Stripe session found for this payment");
        }

        String sessionStatus = stripeCheckoutClient.getSessionStatus(sessionId);
        if ("complete".equals(sessionStatus)) {
            String paymentIntentId = stripeCheckoutClient.getPaymentIntentId(sessionId);
            markPaymentPaid(payment, paymentIntentId);
        } else if ("expired".equals(sessionStatus)) {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
        }

        return toResponse(payment, paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null));
    }

    private Payment validatePaymentExists(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void validatePaymentAccess(UUID requesterUserId, Payment payment) {
        UUID buyerId = payment.getBuyer() == null ? null : payment.getBuyer().getUserId();
        if (requesterUserId != null && requesterUserId.equals(buyerId)) {
            return;
        }
        validateAdmin(requesterUserId);
    }

    private void validateAdmin(UUID requesterUserId) {
        if (requesterUserId == null
                || !userRoleAssignmentRepository.existsByUserUserIdAndRoleName(requesterUserId, ADMIN_ROLE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
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
        try {
            activatePurchasedProduct(payment);
        } catch (Exception ex) {
            log.error("Activation failed for payment {} — initiating refund. Error: {}",
                    payment.getPaymentId(), ex.getMessage());
            if (payment.getPaymentMethod() == com.cafestory.entity.enums.PaymentMethod.STRIPE_CARD
                    && providerTransactionId != null) {
                try {
                    stripeCheckoutClient.refundPaymentIntent(providerTransactionId);
                } catch (Exception refundEx) {
                    log.error("Stripe refund also failed for payment {}: {}",
                            payment.getPaymentId(), refundEx.getMessage());
                }
            }
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }
    }

    private ProductPurchase resolveProductPurchase(CreatePaymentRequestDTO request) {
        boolean hasExtraFee = request.getExtraFeeId() != null;
        boolean hasAdFee = request.getAdFeeId() != null;
        if (hasExtraFee == hasAdFee) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Exactly one of extraFeeId or adFeeId is required");
        }
        if (hasExtraFee) {
            ExtraFee extraFee = extraFeeRepository.findById(request.getExtraFeeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found"));
            if (!Boolean.TRUE.equals(extraFee.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extra fee is inactive");
            }
            return new ProductPurchase(extraFee, null, BigDecimal.valueOf(extraFee.getPrice()), "VND");
        }

        AdFee adFee = adFeeRepository.findById(request.getAdFeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad fee not found"));
        if (!Boolean.TRUE.equals(adFee.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ad fee is inactive");
        }
        String currency = adFee.getCurrency() == null || adFee.getCurrency().isBlank() ? "VND" : adFee.getCurrency();
        return new ProductPurchase(null, adFee, adFee.getPrice(), currency);
    }

    private void markPaymentFailed(Payment payment, Map<String, String> params) {
        if ("24".equals(params.get("vnp_ResponseCode"))) {
            payment.setPaymentStatus(PaymentStatus.CANCELLED);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);
    }

    private void activatePurchasedProduct(Payment payment) {
        ExtraFee extraFee = payment.getExtraFee();
        if (extraFee == null) {
            return;
        }
        if (extraFee.getFeeType() == ExtraFeeType.REVIEWER_REGISTRATION) {
            activateReviewerSubscription(payment.getBuyer(), extraFee);
        } else if (extraFee.getFeeType() == ExtraFeeType.CAFE_PAGE_OPENING) {
            activateCafePagePackage(payment.getBuyer(), extraFee);
        }
    }

    private void activateReviewerSubscription(User buyer, ExtraFee extraFee) {
        if (extraFee.getDurationMonths() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reviewer package durationMonths is required");
        }
        Reviewer reviewer = reviewerRepository.findByUserUserId(buyer.getUserId()).orElseGet(Reviewer::new);
        reviewer.setUser(buyer);
        reviewer.setReviewerActive(true);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpireDate = reviewer.getReviewerExpiresAt();
        LocalDateTime baseExpireDate = currentExpireDate != null && currentExpireDate.isAfter(now)
                ? currentExpireDate
                : now;
        reviewer.setReviewerExpiresAt(baseExpireDate.plusMonths(extraFee.getDurationMonths()));
        reviewerRepository.save(reviewer);
        assignRole(buyer, REVIEWER_ROLE);
    }

    private void activateCafePagePackage(User buyer, ExtraFee extraFee) {
        CafePage cafePage = cafePageRepository.findByOwnerUserId(buyer.getUserId()).stream()
                .findFirst()
                .orElseGet(() -> createCafePageForBuyer(buyer));
        applyCafePagePackage(cafePage, extraFee);
        CafePage savedCafePage = cafePageRepository.save(cafePage);
        ensureOwnerMembership(savedCafePage, buyer);
        assignRole(buyer, CAFE_PAGE_ROLE);
    }

    private CafePage createCafePageForBuyer(User buyer) {
        CafePage cafePage = new CafePage();
        cafePage.setOwner(buyer);
        cafePage.setName(defaultCafePageName(buyer));
        cafePage.setAddress(DEFAULT_CAFE_PAGE_ADDRESS);
        cafePage.setStatus(PageStatus.DRAFT);
        cafePage.setLikeCount(0);
        cafePage.setFollowerCount(0);
        cafePage.setRegion(buyer.getRegion());
        return cafePage;
    }

    private void applyCafePagePackage(CafePage cafePage, ExtraFee extraFee) {
        cafePage.setMaxMembers(extraFee.getMaxMembers() == null
                ? DEFAULT_CAFE_PAGE_MAX_MEMBERS
                : extraFee.getMaxMembers());
        cafePage.setPageActive(true);
        if (extraFee.getDurationMonths() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime currentExpireDate = cafePage.getPageExpiresAt();
            LocalDateTime baseExpireDate = currentExpireDate != null && currentExpireDate.isAfter(now)
                    ? currentExpireDate
                    : now;
            cafePage.setPageExpiresAt(baseExpireDate.plusMonths(extraFee.getDurationMonths()));
        }
    }

    private String defaultCafePageName(User buyer) {
        String displayName = buyer.getUserFullName();
        if (displayName == null || displayName.isBlank()) {
            displayName = buyer.getUserName();
        }
        return displayName + "'s Cafe Page";
    }

    private void ensureOwnerMembership(CafePage cafePage, User buyer) {
        PageMember pageMember = pageMemberRepository.findByCafePageIdAndUserUserId(cafePage.getId(), buyer.getUserId())
                .orElseGet(() -> {
                    PageMember newMember = new PageMember();
                    newMember.setId(new PageMemberId(cafePage.getId(), buyer.getUserId()));
                    newMember.setCafePage(cafePage);
                    newMember.setUser(buyer);
                    return newMember;
                });
        pageMember.setRoleName(PageMember.ROLE_OWNER);
        pageMember.setStatus(PageMemberStatus.ACTIVE);
        pageMemberRepository.save(pageMember);
    }

    private void assignRole(User user, String roleName) {
        if (userRoleAssignmentRepository.existsByUserUserIdAndRoleName(user.getUserId(), roleName)) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
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
        response.setExtraFeeId(payment.getExtraFee() == null ? null : payment.getExtraFee().getExtraFeeId());
        response.setExtraFeeType(payment.getExtraFee() == null ? null : payment.getExtraFee().getFeeType());
        response.setAdFeeId(payment.getAdFee() == null ? null : payment.getAdFee().getAdFeeId());
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

    private void updateVnpayDetail(PaymentDetail detail, Map<String, String> params) {
        if (detail == null) {
            return;
        }
        detail.setProviderTransactionId(params.get("vnp_TransactionNo"));
        detail.setFailureCode(successCode(params) ? null : params.get("vnp_ResponseCode"));
        detail.setFailureMessage(successCode(params) ? null : "VNPAY transaction failed");
        detail.setRawResponse(writeJson(params));
        paymentDetailRepository.save(detail);
    }

    private boolean isVnpaySuccess(Map<String, String> params) {
        return successCode(params) && "00".equals(params.get("vnp_TransactionStatus"));
    }

    private boolean successCode(Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode"));
    }

    private boolean isVnpayPending(Map<String, String> params) {
        return "01".equals(params.get("vnp_TransactionStatus"));
    }

    private boolean amountMatches(Payment payment, String vnpAmount) {
        if (vnpAmount == null || vnpAmount.isBlank() || payment.getAmount() == null) {
            return false;
        }
        try {
            BigDecimal callbackAmount = new BigDecimal(vnpAmount).divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.UNNECESSARY);
            return payment.getAmount().compareTo(callbackAmount) == 0;
        } catch (ArithmeticException | NumberFormatException ex) {
            return false;
        }
    }

    private Optional<UUID> parsePaymentId(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private VnpayReturnResponseDTO vnpayReturn(
            UUID paymentId,
            String status,
            Payment payment,
            Map<String, String> params,
            String message) {
        VnpayReturnResponseDTO response = new VnpayReturnResponseDTO();
        response.setPaymentId(paymentId);
        response.setStatus(status);
        response.setPaymentStatus(payment == null ? null : payment.getPaymentStatus());
        response.setResponseCode(params.get("vnp_ResponseCode"));
        response.setTransactionStatus(params.get("vnp_TransactionStatus"));
        response.setTransactionNo(params.get("vnp_TransactionNo"));
        response.setMessage(message);
        return response;
    }

    private String writeJson(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private record StripeWebhookData(String type, String sessionId, String paymentIntentId) {
    }

    private record ProductPurchase(ExtraFee extraFee, AdFee adFee, BigDecimal amount, String currency) {
    }
}
