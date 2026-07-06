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

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
