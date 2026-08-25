package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ContentReportRequestDTO {
    @NotNull
    private ReportTargetType targetType;

    @NotNull
    private UUID targetId;

    @NotNull
    private UUID reasonId;

    @Size(max = 2000)
    private String description;
}
