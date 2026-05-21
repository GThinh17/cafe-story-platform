package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_details")
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_detail_id", updatable = false, nullable = false)
    private UUID paymentDetailId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "provider_name", length = 80)
    private String providerName;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "provider_order_id")
    private String providerOrderId;

    @Column(name = "provider_payment_url", columnDefinition = "text")
    private String providerPaymentUrl;

    @Column(name = "provider_qr_code_url", columnDefinition = "text")
    private String providerQrCodeUrl;

    @Column(name = "transfer_content")
    private String transferContent;

    @Column(name = "raw_response", columnDefinition = "text")
    private String rawResponse;

    @Column(name = "failure_code", length = 80)
    private String failureCode;

    @Column(name = "failure_message", columnDefinition = "text")
    private String failureMessage;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
