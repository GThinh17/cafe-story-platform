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
    private String reporterUserAvatar;
    private ReportTargetType targetType;
    private UUID targetId;
    private UUID blogId;
    private UUID commentId;
    private UUID reportedUserId;
    private UUID cafePageId;
    private UUID reasonId;
    private String reasonCode;
    private String reason;
    private String reasonLabel;
    private Integer reasonSeverity;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
