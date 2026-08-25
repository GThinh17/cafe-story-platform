package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminContentReportStatusUpdateRequestDTO {
    @NotNull
    private ReportStatus status;
}
