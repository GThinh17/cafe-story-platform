package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.service.serviceInterface.PaymentHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentHistoryServiceImpl implements PaymentHistoryService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;

    public PaymentHistoryServiceImpl(
            PaymentRepository paymentRepository,
            PaymentDetailRepository paymentDetailRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPayments(UUID requesterUserId, PaymentStatus paymentStatus) {
        List<Payment> payments = paymentStatus == null
                ? paymentRepository.findByBuyerUserIdOrderByCreatedAtDesc(requesterUserId)
                : paymentRepository.findByBuyerUserIdAndPaymentStatusOrderByCreatedAtDesc(
                        requesterUserId,
                        paymentStatus);

        return payments.stream()
                .map(payment -> toResponse(
                        payment,
                        paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()).orElse(null)))
                .toList();
    }

    private PaymentResponseDTO toResponse(Payment payment, PaymentDetail detail) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(payment.getPaymentId());
        response.setBuyerId(payment.getBuyer().getUserId());
        response.setExtraFeeId(payment.getExtraFee() == null ? null : payment.getExtraFee().getExtraFeeId());
        response.setExtraFeeType(payment.getExtraFee() == null ? null : payment.getExtraFee().getFeeType());
        response.setAdFeeId(payment.getAdFee() == null ? null : payment.getAdFee().getAdFeeId());
        response.setActivatedCafePageId(payment.getCafePage() == null ? null : payment.getCafePage().getId());
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
