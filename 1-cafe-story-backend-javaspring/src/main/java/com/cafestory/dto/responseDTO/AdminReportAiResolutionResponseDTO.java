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

    private Map<String, Object> rawResponse;

    private LocalDateTime createdAt;
}
