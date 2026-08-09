package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AdminReportAiResolutionRequestDTO {

    public AdminReportAiResolutionRequestDTO() {
    }

    public AdminReportAiResolutionRequestDTO(
            UUID reportId,
            ReportTargetType targetType,
            UUID targetId,
            String reasonCode,
            String reasonLabel,
            Integer reasonSeverity,
            String description,
            String contentText,
            List<String> imageUrls,
            ReportStatus reportStatus,
            Map<String, Object> existingModerationResult,
            Long sameTargetOpenReportCount) {
        this.reportId = reportId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reasonCode = reasonCode;
        this.reasonLabel = reasonLabel;
        this.reasonSeverity = reasonSeverity;
        this.description = description;
        this.contentText = contentText;
        this.imageUrls = imageUrls;
        this.reportStatus = reportStatus;
        this.existingModerationResult = existingModerationResult;
        this.sameTargetOpenReportCount = sameTargetOpenReportCount;
    }

    private String contractVersion;

    private UUID correlationId;

    private String idempotencyKey;

    private OffsetDateTime requestedAt;

    private String automationMode;

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

    private Map<String, Object> reportClaim;

    private Map<String, Object> targetSnapshot;

    private List<AdminReportAiEvidenceItemRequestDTO> evidence;

    private AdminReportAiPolicyContextRequestDTO policyContext;

    private Map<String, Object> executionConstraints;
}
