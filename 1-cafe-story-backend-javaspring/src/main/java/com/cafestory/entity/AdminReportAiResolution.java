package com.cafestory.entity;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "admin_report_ai_resolutions",
        indexes = {
                @Index(
                        name = "idx_admin_report_ai_resolutions_report_created",
                        columnList = "content_report_id, created_at")
        })
public class AdminReportAiResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "contract_version", length = 32)
    private String contractVersion;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "automation_mode", length = 40)
    private String automationMode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_report_id", nullable = false)
    private ContentReport contentReport;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "labels", columnDefinition = "jsonb")
    private List<String> labels;

    @Column(name = "rule_code", length = 120)
    private String ruleCode;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "model_name", length = 120)
    private String modelName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "jsonb")
    private Map<String, Object> rawResponse;

    @Column(name = "policy_version", length = 80)
    private String policyVersion;

    @Column(name = "rule_catalog_version", length = 80)
    private String ruleCatalogVersion;

    @Column(name = "prompt_version", length = 80)
    private String promptVersion;

    @Column(name = "workflow_version", length = 80)
    private String workflowVersion;

    @Column(name = "target_snapshot_hash", length = 64)
    private String targetSnapshotHash;

    @Column(name = "evidence_quality", length = 24)
    private String evidenceQuality;

    @Column(name = "evidence_sufficiency", length = 24)
    private String evidenceSufficiency;

    @Column(name = "violation_likelihood", length = 24)
    private String violationLikelihood;

    @Column(name = "harm_severity", length = 24)
    private String harmSeverity;

    @Column(name = "action_risk", length = 24)
    private String actionRisk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "findings_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> findings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_summary_json", columnDefinition = "jsonb")
    private Map<String, Object> evidenceSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blocked_reasons_json", columnDefinition = "jsonb")
    private List<String> blockedReasons;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
