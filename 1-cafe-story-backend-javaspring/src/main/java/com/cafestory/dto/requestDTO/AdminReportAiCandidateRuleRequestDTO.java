package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportAiCandidateRuleRequestDTO {

    private String ruleId;

    private String ruleVersion;

    private String ruleStatus;

    private String ruleFamily;

    private String ruleType;

    private Boolean material;

    private List<ReportTargetType> applicableTargetTypes;

    private List<String> requirementProfileIds;

    private List<String> requiredEvidenceKinds;

    private List<AdminReportAiRuleRequirementRequestDTO> conditionalRequirements;

    private List<String> semanticRequirementCodes;

    private Boolean counterEvidenceRequired;

    private List<String> exceptionCodes;

    private String evaluationCeiling;

    private List<String> allowedOutcomes;

    private List<String> allowedCandidateActions;
}
