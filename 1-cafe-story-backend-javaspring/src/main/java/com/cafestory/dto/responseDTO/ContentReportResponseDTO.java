package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ContentReportResponseDTO {
    private UUID id;
    private UUID reporterUserId;
    private String reporterUserName;
    private ReportTargetType targetType;
    private UUID targetId;
    private UUID blogId;
    private UUID commentId;
    private UUID reportedUserId;
    private UUID cafePageId;
    private String reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
