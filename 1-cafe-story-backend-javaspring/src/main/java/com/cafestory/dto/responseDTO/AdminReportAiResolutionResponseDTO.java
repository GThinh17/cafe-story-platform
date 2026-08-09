package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class AdminReportAiResolutionResponseDTO {

    private UUID id;

    private String contractVersion;

    private UUID correlationId;

    private String automationMode;

    private String recommendationState;

    private UUID contentReportId;

    private ReportTargetType targetType;

    private UUID targetId;

    private AdminReportAiReportDecision reportDecision;

    private AdminReportAiTargetAction targetAction;

    private Double confidenceScore;

    private Double riskScore;

    private List<String> labels;

    private String ruleCode;

    private String explanation;

    private String modelName;

    private LocalDateTime createdAt;

    private AdminReportAiAutoApplyJobResponseDTO autoApplyJob;

    private String autoApplyWarning;

    private List<Map<String, Object>> findings;

    private Map<String, Object> evidenceSummary;

    private List<String> blockedReasons;

    private String evidenceQuality;

    private String evidenceSufficiency;

    private String violationLikelihood;

    private String harmSeverity;

    private String actionRisk;

    private String policyVersion;

    private String ruleCatalogVersion;

    private String promptVersion;

    private String workflowVersion;

    private String targetSnapshotHash;
}
