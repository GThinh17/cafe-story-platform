package com.cafestory.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportAiMissingRequirementRequestDTO {

    private String missingRequirementId;

    private String ruleId;

    private String requirementCode;

    private String evidenceKind;

    private String status;

    private String reasonCode;
}
