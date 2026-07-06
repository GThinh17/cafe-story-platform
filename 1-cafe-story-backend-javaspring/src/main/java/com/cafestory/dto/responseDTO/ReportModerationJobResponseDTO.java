package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReportModerationJobStatus;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportModerationJobResponseDTO {

    private UUID id;

    private UUID contentReportId;

    private ReportTargetType targetType;

    private UUID targetId;

    private ReportModerationJobStatus status;

    private Integer attemptCount;

    private Integer maxAttempts;

    private LocalDateTime nextAttemptAt;

    private LocalDateTime processingStartedAt;

    private String lastError;

    private Long lastDurationMs;

    private Double priorityScore;

    private Double riskScore;

    private Double reasonSeveritySignal;

    private Double reportCountSignal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
