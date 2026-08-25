package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportReasonResponseDTO {
    private UUID id;
    private String code;
    private String labelVi;
    private String descriptionVi;
    private ReportTargetType targetType;
    private Integer severity;
    private Boolean requiresDescription;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
