package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AdminReportAiResolutionRequestDTO {

    private UUID reportId;

    private ReportTargetType targetType;

    private UUID targetId;

    private String reasonCode;

    private String reasonLabel;

    private Integer reasonSeverity;

    private String description;

    private String contentText;

    private List<String> imageUrls;

    private ReportStatus reportStatus;

    private Map<String, Object> existingModerationResult;

    private Long sameTargetOpenReportCount;
}
