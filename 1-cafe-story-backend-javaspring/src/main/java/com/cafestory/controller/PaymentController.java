package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.dto.responseDTO.VnpayReturnResponseDTO;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceInterface.PaymentService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponseDTO createPayment(
            @Valid @RequestBody CreatePaymentRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return paymentService.createPayment(requireUserId(principal), request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return paymentService.getPayment(requireUserId(principal), paymentId);
    }

    @GetMapping
    public List<PaymentResponseDTO> getAllPayments(
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return paymentService.getAllPayments(requireUserId(principal), paymentStatus);
    }

    @PostMapping("/{paymentId}/bank-transfer/mark-paid")
    public PaymentResponseDTO markBankTransferPaid(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return paymentService.markBankTransferPaid(requireUserId(principal), paymentId);
    }

    @PostMapping("/{paymentId}/stripe/sync")
    public PaymentResponseDTO syncStripePayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return paymentService.syncStripePayment(requireUserId(principal), paymentId);
    }

    @PostMapping("/stripe/webhook")
    public void handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader) {
        paymentService.handleStripeWebhook(payload, signatureHeader);
    }

    @GetMapping("/vnpay/return")
    public VnpayReturnResponseDTO handleVnpayReturn(@RequestParam Map<String, String> params) {
        return paymentService.handleVnpayReturn(params);
    }

    @GetMapping("/vnpay/ipn")
    public VnpayIpnResponseDTO handleVnpayIpn(@RequestParam Map<String, String> params) {
        return paymentService.handleVnpayIpn(params);
    }
}
