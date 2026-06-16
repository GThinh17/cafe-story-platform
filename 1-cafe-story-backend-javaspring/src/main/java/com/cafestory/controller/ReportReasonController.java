package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReportReasonResponseDTO;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.ReportReasonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report-reasons")
public class ReportReasonController {

    private final ReportReasonService reportReasonService;

    public ReportReasonController(ReportReasonService reportReasonService) {
        this.reportReasonService = reportReasonService;
    }

    @GetMapping
    public List<ReportReasonResponseDTO> getActiveReportReasons(
            @RequestParam(required = false) ReportTargetType targetType) {
        return reportReasonService.getActiveReportReasons(targetType);
    }
}
