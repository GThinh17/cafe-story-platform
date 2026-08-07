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
@Table(name = "reviewers")
public class Reviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reviewer_id", updatable = false, nullable = false)
    private UUID reviewerId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reviewer_expires_at")
    private LocalDateTime reviewerExpiresAt;

    @Column(name = "reviewer_active", nullable = false)
    private Boolean reviewerActive = false;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (reviewerActive == null) {
            reviewerActive = false;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Gói reviewer còn hiệu lực hay không.
     *
     * <p>Cờ {@code reviewerActive} một mình là không đủ: nó chỉ được bật khi
     * thanh toán thành công và phụ thuộc job hạ cờ chạy đúng giờ. Mọi chỗ chặn
     * quyền phải hỏi hàm này để một job lỡ nhịp không biến thành gói vô hạn.
     */
    public boolean isSubscriptionActive() {
        return Boolean.TRUE.equals(reviewerActive)
                && (reviewerExpiresAt == null || reviewerExpiresAt.isAfter(LocalDateTime.now()));
    }
}
