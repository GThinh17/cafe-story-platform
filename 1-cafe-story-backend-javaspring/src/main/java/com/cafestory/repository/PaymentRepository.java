package com.cafestory.repository;

import com.cafestory.entity.Payment;
import com.cafestory.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByOrderByCreatedAtDesc();

    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);
}
