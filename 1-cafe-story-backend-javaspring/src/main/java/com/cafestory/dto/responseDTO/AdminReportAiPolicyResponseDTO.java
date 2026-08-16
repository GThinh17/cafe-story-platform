package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportAiPolicyResponseDTO {

    private UUID reportId;

    private ReportTargetType targetType;

    private String reasonCode;

    private String contextSchemaVersion;

    private String policyVersion;

    private String policyStatus;

    private String ruleCatalogVersion;

    private String ruleCatalogStatus;

    private String evaluationMode;

    private Boolean recommendationOnly;

    private List<AdminReportAiPolicyRuleResponseDTO> candidateRules;
}
