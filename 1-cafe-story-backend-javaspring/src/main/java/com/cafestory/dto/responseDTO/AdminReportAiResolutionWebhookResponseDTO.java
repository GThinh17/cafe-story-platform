package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdminReportAiResolutionWebhookResponseDTO {

    private AdminReportAiReportDecision reportDecision;

    private AdminReportAiTargetAction targetAction;

    private Double confidenceScore;

    private Double riskScore;

    private List<String> labels;

    private String ruleCode;

    private String explanation;

    private String modelName;

    private Map<String, Object> rawResponse;
}
