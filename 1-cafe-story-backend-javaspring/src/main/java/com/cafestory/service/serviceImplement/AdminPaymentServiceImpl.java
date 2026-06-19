package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.service.serviceInterface.AdminPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;

    public AdminPaymentServiceImpl(PaymentRepository paymentRepository, PaymentDetailRepository paymentDetailRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPayments(PaymentStatus paymentStatus, UUID buyerId, Pageable pageable) {
        return paymentRepository.findAdminPayments(paymentStatus, buyerId, pageable)
                .map(payment -> toResponse(payment, findDetail(payment.getPaymentId())));
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
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);
        return toResponse(savedPayment, findDetail(paymentId));
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
        response.setBuyerUserAvatar(payment.getBuyer() == null ? null : payment.getBuyer().getUserAvatar());
        response.setExtraFeeId(payment.getExtraFee() == null ? null : payment.getExtraFee().getExtraFeeId());
        response.setAdFeeId(payment.getAdFee() == null ? null : payment.getAdFee().getAdFeeId());
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
