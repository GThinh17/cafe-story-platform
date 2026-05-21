package com.cafestory.repository;

import com.cafestory.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, UUID> {

    Optional<PaymentDetail> findByPaymentPaymentId(UUID paymentId);

    Optional<PaymentDetail> findByProviderOrderId(String providerOrderId);
}
