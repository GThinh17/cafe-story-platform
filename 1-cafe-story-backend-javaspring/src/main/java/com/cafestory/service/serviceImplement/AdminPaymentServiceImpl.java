package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.service.serviceInterface.AdminPaymentService;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private static final Logger log = LoggerFactory.getLogger(AdminPaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final StripeCheckoutClient stripeCheckoutClient;

    public AdminPaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentDetailRepository paymentDetailRepository,
            StripeCheckoutClient stripeCheckoutClient) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.stripeCheckoutClient = stripeCheckoutClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPayments(PaymentStatus paymentStatus, UUID buyerId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAdminPayments(paymentStatus, buyerId, pageable);
        List<UUID> paymentIds = payments.stream().map(Payment::getPaymentId).toList();
        Map<UUID, PaymentDetail> detailsByPaymentId = paymentIds.isEmpty()
                ? Map.of()
                : paymentDetailRepository.findByPaymentPaymentIdIn(paymentIds).stream()
                        .collect(Collectors.toMap(
                                detail -> detail.getPayment().getPaymentId(),
                                detail -> detail));
        return payments.map(payment -> toResponse(payment, detailsByPaymentId.get(payment.getPaymentId())));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        return toResponse(payment, findDetail(paymentId));
    }

    @Override
    @Transactional
    public PaymentResponseDTO markBankTransferPaid(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is not a bank transfer");
        }
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Refunded payment cannot be marked paid");
        }
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        return toResponse(savedPayment, findDetail(paymentId));
    }

    @Override
    @Transactional
    public PaymentResponseDTO refundPayment(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only paid payments can be refunded");
        }
        if (payment.getPaymentMethod() == PaymentMethod.STRIPE_CARD) {
            PaymentDetail detail = findDetail(paymentId);
            if (detail != null && detail.getProviderTransactionId() != null) {
                stripeCheckoutClient.refundPaymentIntent(detail.getProviderTransactionId());
            }
        }
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);
        return toResponse(savedPayment, findDetail(paymentId));
    }

    @Override
    @Transactional
    public int expireStalePayments() {
        List<Payment> stale = paymentRepository
                .findByPaymentStatusAndExpiredAtBefore(PaymentStatus.PENDING, LocalDateTime.now().minusMinutes(1));
        int count = 0;
        for (Payment payment : stale) {
            try {
                if (payment.getPaymentMethod() == PaymentMethod.STRIPE_CARD) {
                    PaymentDetail detail = findDetail(payment.getPaymentId());
                    String sessionId = detail != null ? detail.getProviderOrderId() : null;
                    if (sessionId != null) {
                        String sessionStatus = stripeCheckoutClient.getSessionStatus(sessionId);
                        if ("complete".equals(sessionStatus)) {
                            // User đã trả tiền nhưng activation fail → refund
                            String paymentIntentId = detail.getProviderTransactionId();
                            if (paymentIntentId != null) {
                                stripeCheckoutClient.refundPaymentIntent(paymentIntentId);
                                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                            } else {
                                payment.setPaymentStatus(PaymentStatus.EXPIRED);
                            }
                        } else if ("open".equals(sessionStatus)) {
                            stripeCheckoutClient.expireSession(sessionId);
                            payment.setPaymentStatus(PaymentStatus.EXPIRED);
                        } else {
                            // expired/unknown — chỉ update DB
                            payment.setPaymentStatus(PaymentStatus.EXPIRED);
                        }
                    } else {
                        payment.setPaymentStatus(PaymentStatus.EXPIRED);
                    }
                } else {
                    payment.setPaymentStatus(PaymentStatus.EXPIRED);
                }
                paymentRepository.save(payment);
                count++;
            } catch (Exception ex) {
                log.error("Failed to expire payment {}: {}", payment.getPaymentId(), ex.getMessage());
            }
        }
        log.info("expireStalePayments: processed {} stale payments", count);
        return count;
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private PaymentDetail findDetail(UUID paymentId) {
        return paymentDetailRepository.findByPaymentPaymentId(paymentId).orElse(null);
    }

    private PaymentResponseDTO toResponse(Payment payment, PaymentDetail detail) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(payment.getPaymentId());
        response.setBuyerId(payment.getBuyer() == null ? null : payment.getBuyer().getUserId());
        response.setBuyerUserName(payment.getBuyer() == null ? null : payment.getBuyer().getUserName());
        response.setBuyerUserFullName(payment.getBuyer() == null ? null : payment.getBuyer().getUserFullName());
        response.setBuyerUserAvatar(payment.getBuyer() == null ? null : payment.getBuyer().getUserAvatar());
        response.setExtraFeeId(payment.getExtraFee() == null ? null : payment.getExtraFee().getExtraFeeId());
        response.setAdFeeId(payment.getAdFee() == null ? null : payment.getAdFee().getAdFeeId());
        response.setActivatedCafePageId(payment.getCafePage() == null ? null : payment.getCafePage().getId());
        if (payment.getExtraFee() != null) {
            response.setProductName(payment.getExtraFee().getName());
        } else if (payment.getAdFee() != null) {
            response.setProductName(payment.getAdFee().getFeeType().name());
        }
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
}
