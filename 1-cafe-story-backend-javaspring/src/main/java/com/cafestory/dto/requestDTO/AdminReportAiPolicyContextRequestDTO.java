package com.cafestory.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportAiPolicyContextRequestDTO {

    private String contextSchemaVersion;

    private String policyVersion;

    private String policyStatus;

    private String ruleCatalogVersion;

    private String ruleCatalogStatus;

    private String requirementMatrixVersion;

    private String evidenceKindCatalogVersion;

    private String evaluationMode;

    private List<AdminReportAiCandidateRuleRequestDTO> candidateRules;

    private List<String> availableEvidenceKinds;

    private List<AdminReportAiMissingRequirementRequestDTO> missingRequirements;

    private String currentEvaluationCeiling;
}
