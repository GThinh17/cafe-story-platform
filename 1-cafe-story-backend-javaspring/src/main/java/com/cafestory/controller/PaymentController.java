package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.service.serviceInterface.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponseDTO createPayment(@Valid @RequestBody CreatePaymentRequestDTO request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPayment(@PathVariable UUID paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @PostMapping("/{paymentId}/bank-transfer/mark-paid")
    public PaymentResponseDTO markBankTransferPaid(@PathVariable UUID paymentId) {
        return paymentService.markBankTransferPaid(paymentId);
    }

    @PostMapping("/stripe/webhook")
    public void handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader) {
        paymentService.handleStripeWebhook(payload, signatureHeader);
    }
}
