package com.cafestory.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportAiPolicyRuleRequirementResponseDTO {

    private String requirementCode;

    private String evidenceKind;

    private String requirementType;

    private String trigger;

    private String missingBehavior;
}
