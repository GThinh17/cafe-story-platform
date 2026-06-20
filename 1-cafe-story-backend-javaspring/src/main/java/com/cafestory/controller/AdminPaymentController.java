package com.cafestory.controller;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceInterface.AdminPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    public AdminPaymentController(AdminPaymentService adminPaymentService) {
        this.adminPaymentService = adminPaymentService;
    }

    @GetMapping
    public Page<PaymentResponseDTO> getPayments(
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminPaymentService.getPayments(paymentStatus, buyerId, pageable(page, size));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPayment(@PathVariable UUID paymentId) {
        return adminPaymentService.getPayment(paymentId);
    }

    @PostMapping("/{paymentId}/bank-transfer/mark-paid")
    public PaymentResponseDTO markBankTransferPaid(@PathVariable UUID paymentId) {
        return adminPaymentService.markBankTransferPaid(paymentId);
    }

    @PostMapping("/{paymentId}/refund")
    public PaymentResponseDTO refundPayment(@PathVariable UUID paymentId) {
        return adminPaymentService.refundPayment(paymentId);
    }

    @PostMapping("/expire-stale")
    public int expireStalePayments() {
        return adminPaymentService.expireStalePayments();
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
