package com.cafestory.repository;

import com.cafestory.entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, UUID> {

    Optional<PaymentDetail> findByPaymentPaymentId(UUID paymentId);

    List<PaymentDetail> findByPaymentPaymentIdIn(Collection<UUID> paymentIds);

    Optional<PaymentDetail> findByProviderOrderId(String providerOrderId);
}
