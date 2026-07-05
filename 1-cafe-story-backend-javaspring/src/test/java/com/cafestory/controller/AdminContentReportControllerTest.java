package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.cafestory.service.serviceInterface.ContentReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminContentReportControllerTest {

    @Mock
    private ContentReportService contentReportService;

    @Mock
    private AdminReportAiResolutionService adminReportAiResolutionService;

    @InjectMocks
    private AdminContentReportController adminContentReportController;

    @Test
    void getReports_success_TC001() {
        Page<ContentReportResponseDTO> response = new PageImpl<>(List.of(response()));

        when(contentReportService.getReports(
                eq(ReportStatus.OPEN),
                eq(ReportTargetType.BLOG),
                any(Pageable.class))).thenReturn(response);

        Page<ContentReportResponseDTO> result = adminContentReportController.getReports(
                ReportStatus.OPEN,
                ReportTargetType.BLOG,
                0,
                20);

        assertThat(result).isEqualTo(response);
        verify(contentReportService).getReports(
                eq(ReportStatus.OPEN),
                eq(ReportTargetType.BLOG),
                any(Pageable.class));
    }

    @Test
    void updateStatus_success_TC002() {
        UUID reportId = UUID.randomUUID();
        AdminContentReportStatusUpdateRequestDTO request = new AdminContentReportStatusUpdateRequestDTO();
        request.setStatus(ReportStatus.RESOLVED);
        ContentReportResponseDTO response = response();

        when(contentReportService.updateStatus(reportId, request)).thenReturn(response);

        ContentReportResponseDTO result = adminContentReportController.updateStatus(reportId, request);

        assertThat(result).isEqualTo(response);
        verify(contentReportService).updateStatus(reportId, request);
    }

    @Test
    void resolveReport_successDelegatesToContentReportService_TC003() {
        UUID reportId = UUID.randomUUID();
        ContentReportResponseDTO response = response();
        response.setStatus(ReportStatus.RESOLVED);

        when(contentReportService.resolveReport(reportId)).thenReturn(response);

        ContentReportResponseDTO result = adminContentReportController.resolveReport(reportId);

        assertThat(result).isEqualTo(response);
        verify(contentReportService).resolveReport(reportId);
    }

    @Test
    void createAiResolution_successDelegatesToAiResolutionService_TC004() {
        UUID reportId = UUID.randomUUID();
        AdminReportAiResolutionResponseDTO response = aiResolutionResponse(reportId);

        when(adminReportAiResolutionService.createResolution(reportId)).thenReturn(response);

        AdminReportAiResolutionResponseDTO result = adminContentReportController.createAiResolution(reportId);

        assertThat(result).isEqualTo(response);
        verify(adminReportAiResolutionService).createResolution(reportId);
    }

    @Test
    void getAiResolutions_successDelegatesToAiResolutionService_TC005() {
        UUID reportId = UUID.randomUUID();
        Page<AdminReportAiResolutionResponseDTO> response = new PageImpl<>(List.of(aiResolutionResponse(reportId)));

        when(adminReportAiResolutionService.getResolutions(eq(reportId), any(Pageable.class))).thenReturn(response);

        Page<AdminReportAiResolutionResponseDTO> result = adminContentReportController.getAiResolutions(
                reportId,
                0,
                20);

        assertThat(result).isEqualTo(response);
        verify(adminReportAiResolutionService).getResolutions(eq(reportId), any(Pageable.class));
    }

    private ContentReportResponseDTO response() {
        ContentReportResponseDTO response = new ContentReportResponseDTO();
        response.setId(UUID.randomUUID());
        response.setStatus(ReportStatus.OPEN);
        return response;
    }

    private AdminReportAiResolutionResponseDTO aiResolutionResponse(UUID reportId) {
        AdminReportAiResolutionResponseDTO response = new AdminReportAiResolutionResponseDTO();
        response.setId(UUID.randomUUID());
        response.setContentReportId(reportId);
        response.setTargetType(ReportTargetType.BLOG);
        response.setTargetId(UUID.randomUUID());
        response.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        response.setTargetAction(AdminReportAiTargetAction.NONE);
        response.setConfidenceScore(62.0);
        response.setRiskScore(41.0);
        response.setModelName("gpt-4o-mini");
        return response;
    }
}
