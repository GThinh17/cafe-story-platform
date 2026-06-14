package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.ReportReasonResponseDTO;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.ReportReasonRepository;
import com.cafestory.service.serviceInterface.ReportReasonService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ReportReasonServiceImpl implements ReportReasonService {

    private final ReportReasonRepository reportReasonRepository;

    public ReportReasonServiceImpl(ReportReasonRepository reportReasonRepository) {
        this.reportReasonRepository = reportReasonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.REPORT_REASONS_CACHE, key = "#p0 == null ? 'all' : #p0.name()")
    public List<ReportReasonResponseDTO> getActiveReportReasons(ReportTargetType targetType) {
        List<ReportReason> reasons = targetType == null
                ? reportReasonRepository.findByActiveTrueOrderBySeverityDescSortOrderAscLabelViAsc()
                : reportReasonRepository.findActiveReasonsForTargetType(targetType);
        return reasons.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportReason validateActiveReportReason(UUID reasonId, ReportTargetType targetType) {
        if (reasonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report reason id is required");
        }
        ReportReason reason = reportReasonRepository.findById(reasonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report reason not found"));
        if (!Boolean.TRUE.equals(reason.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report reason is inactive");
        }
        if (reason.getTargetType() != null && reason.getTargetType() != targetType) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report reason does not match target type");
        }
        return reason;
    }

    private ReportReasonResponseDTO toResponse(ReportReason reason) {
        ReportReasonResponseDTO response = new ReportReasonResponseDTO();
        response.setId(reason.getId());
        response.setCode(reason.getCode());
        response.setLabelVi(reason.getLabelVi());
        response.setDescriptionVi(reason.getDescriptionVi());
        response.setTargetType(reason.getTargetType());
        response.setSeverity(reason.getSeverity());
        response.setRequiresDescription(reason.getRequiresDescription());
        response.setIsActive(reason.getActive());
        response.setSortOrder(reason.getSortOrder());
        response.setCreatedAt(reason.getCreatedAt());
        response.setUpdatedAt(reason.getUpdatedAt());
        return response;
    }
}
