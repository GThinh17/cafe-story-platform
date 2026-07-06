package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminReportAiAutoApplyJobResponseDTO {

    private UUID id;

    private UUID contentReportId;

    private UUID aiResolutionId;

    private ReportTargetType targetType;

    private UUID targetId;

    private AdminReportAiAutoApplyJobStatus status;

    private AdminReportAiReportDecision reportDecision;

    private AdminReportAiTargetAction targetAction;

    private Double confidenceScore;

    private Double riskScore;

    private LocalDateTime scheduledAt;

    private LocalDateTime appliedAt;

    private LocalDateTime cancelledAt;

    private String lastError;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
