package com.cafestory.repository;

import com.cafestory.entity.Payment;
import com.cafestory.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByOrderByCreatedAtDesc();

    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);

    List<Payment> findByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);

    List<Payment> findByBuyerUserIdAndPaymentStatusOrderByCreatedAtDesc(
            UUID buyerUserId, PaymentStatus paymentStatus);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    @Query("""
            select p
            from Payment p
            where (:paymentStatus is null or p.paymentStatus = :paymentStatus)
            and (:buyerId is null or p.buyer.userId = :buyerId)
            """)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"buyer", "extraFee", "adFee", "cafePage"})
    Page<Payment> findAdminPayments(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("buyerId") UUID buyerId,
            Pageable pageable);

    List<Payment> findByPaymentStatusAndExpiredAtBefore(PaymentStatus paymentStatus, LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentId = :id")
    Optional<Payment> findByIdWithLock(@Param("id") UUID id);

    @Query("""
            select coalesce(p.paidAt, p.createdAt), ef.feeType, af.adFeeId, p.amount
            from Payment p
            left join p.extraFee ef
            left join p.adFee af
            where p.paymentStatus = com.cafestory.entity.enums.PaymentStatus.PAID
            and coalesce(p.paidAt, p.createdAt) >= :start
            """)
    List<Object[]> findPaidRevenueRows(@Param("start") LocalDateTime start);
}
