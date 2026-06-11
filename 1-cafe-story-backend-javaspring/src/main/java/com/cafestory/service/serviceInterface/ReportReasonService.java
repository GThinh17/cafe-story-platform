package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ReportReasonResponseDTO;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.enums.ReportTargetType;

import java.util.List;
import java.util.UUID;

public interface ReportReasonService {
    List<ReportReasonResponseDTO> getActiveReportReasons(ReportTargetType targetType);

    ReportReason validateActiveReportReason(UUID reasonId, ReportTargetType targetType);
}
