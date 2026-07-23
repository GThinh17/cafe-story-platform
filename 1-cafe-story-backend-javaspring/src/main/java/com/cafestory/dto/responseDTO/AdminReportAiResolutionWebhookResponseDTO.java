package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class AdminReportAiResolutionWebhookResponseDTO {

    private String contractVersion;

    private UUID correlationId;

    private String recommendationState;

    private AdminReportAiReportDecision reportDecision;

    private AdminReportAiTargetAction targetAction;

    private Double confidenceScore;

    private Double riskScore;

    private List<String> labels;

    private String ruleCode;

    private String explanation;

    private String modelName;

    private Map<String, Object> rawResponse;

    private List<Map<String, Object>> findings;

    private Map<String, Object> evidenceSummary;

    private List<String> blockedReasons;

    private String evidenceQuality;

    private String evidenceSufficiency;

    private String violationLikelihood;

    private String harmSeverity;

    private String policyVersion;

    private String ruleCatalogVersion;

    private String promptVersion;

    private String workflowVersion;
}
