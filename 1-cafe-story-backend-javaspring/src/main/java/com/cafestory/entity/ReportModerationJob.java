package com.cafestory.entity;

import com.cafestory.entity.enums.ReportModerationJobStatus;
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
        name = "report_moderation_jobs",
        indexes = {
                @Index(name = "idx_report_moderation_jobs_status_next_attempt", columnList = "status, next_attempt_at"),
                @Index(name = "idx_report_moderation_jobs_report", columnList = "content_report_id")
        })
public class ReportModerationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_report_id", nullable = false)
    private ContentReport contentReport;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReportModerationJobStatus status = ReportModerationJobStatus.PENDING;

    @NotNull
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @NotNull
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @NotNull
    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "last_duration_ms")
    private Long lastDurationMs;

    @NotNull
    @Column(name = "priority_score", nullable = false)
    private Double priorityScore = 0.0;

    @NotNull
    @Column(name = "risk_score", nullable = false)
    private Double riskScore = 0.0;

    @NotNull
    @Column(name = "reason_severity_signal", nullable = false)
    private Double reasonSeveritySignal = 0.0;

    @NotNull
    @Column(name = "report_count_signal", nullable = false)
    private Double reportCountSignal = 0.0;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (nextAttemptAt == null) {
            nextAttemptAt = createdAt;
        }
        if (status == null) {
            status = ReportModerationJobStatus.PENDING;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
        if (maxAttempts == null) {
            maxAttempts = 3;
        }
        if (priorityScore == null) {
            priorityScore = 0.0;
        }
        if (riskScore == null) {
            riskScore = 0.0;
        }
        if (reasonSeveritySignal == null) {
            reasonSeveritySignal = 0.0;
        }
        if (reportCountSignal == null) {
            reportCountSignal = 0.0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
