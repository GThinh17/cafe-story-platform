package com.cafestory.entity;

import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "admin_report_ai_auto_apply_jobs",
        indexes = {
                @Index(name = "idx_admin_report_ai_auto_jobs_report_created", columnList = "content_report_id, created_at"),
                @Index(name = "idx_admin_report_ai_auto_jobs_status_scheduled", columnList = "status, scheduled_at")
        })
public class AdminReportAiAutoApplyJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_report_id", nullable = false)
    private ContentReport contentReport;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_resolution_id", nullable = false)
    private AdminReportAiResolution aiResolution;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private ReportTargetType targetType;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "report_decision", nullable = false, length = 50)
    private AdminReportAiReportDecision reportDecision;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_action", nullable = false, length = 50)
    private AdminReportAiTargetAction targetAction;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "risk_score")
    private Double riskScore;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AdminReportAiAutoApplyJobStatus status = AdminReportAiAutoApplyJobStatus.SCHEDULED;

    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_by_admin_user_id")
    private UUID createdByAdminUserId;

    @Column(name = "cancelled_by_admin_user_id")
    private UUID cancelledByAdminUserId;

    @Column(name = "cancellation_reason", length = 160)
    private String cancellationReason;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = AdminReportAiAutoApplyJobStatus.SCHEDULED;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
