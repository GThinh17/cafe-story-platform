package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiPolicyResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
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

    @Mock
    private AdminReportAiAutoApplyJobService autoApplyJobService;

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
        UUID adminUserId = UUID.randomUUID();
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        AdminReportAiResolutionResponseDTO response = aiResolutionResponse(reportId);

        when(adminReportAiResolutionService.createResolution(reportId, request, adminUserId)).thenReturn(response);

        AdminReportAiResolutionResponseDTO result = adminContentReportController.createAiResolution(
                reportId,
                request,
                principal(adminUserId));

        assertThat(result).isEqualTo(response);
        verify(adminReportAiResolutionService).createResolution(reportId, request, adminUserId);
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

    @Test
    void getAiAutoResolutions_successDelegatesToAutoApplyService_TC006() {
        UUID reportId = UUID.randomUUID();
        Page<AdminReportAiAutoApplyJobResponseDTO> response = new PageImpl<>(List.of(autoApplyJobResponse(reportId)));

        when(autoApplyJobService.getJobs(eq(reportId), any(Pageable.class))).thenReturn(response);

        Page<AdminReportAiAutoApplyJobResponseDTO> result =
                adminContentReportController.getAiAutoResolutions(reportId, 0, 20);

        assertThat(result).isEqualTo(response);
        verify(autoApplyJobService).getJobs(eq(reportId), any(Pageable.class));
    }

    @Test
    void cancelAiAutoResolution_successDelegatesToAutoApplyService_TC007() {
        UUID jobId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        AdminReportAiAutoApplyJobResponseDTO response = autoApplyJobResponse(UUID.randomUUID());

        when(autoApplyJobService.cancelJob(jobId, adminUserId)).thenReturn(response);

        AdminReportAiAutoApplyJobResponseDTO result =
                adminContentReportController.cancelAiAutoResolution(jobId, principal(adminUserId));

        assertThat(result).isEqualTo(response);
        verify(autoApplyJobService).cancelJob(jobId, adminUserId);
    }

    @Test
    void getAiPolicy_successDelegatesAndPreservesResponseShape_TC008() {
        UUID reportId = UUID.randomUUID();
        AdminReportAiPolicyResponseDTO response = AdminReportAiPolicyResponseDTO.builder()
                .reportId(reportId)
                .targetType(ReportTargetType.COMMENT)
                .reasonCode("SCAM_OR_FRAUD")
                .contextSchemaVersion("RRC-1.0.0-rc.1")
                .policyVersion("PF-2.0.0-proposed.1")
                .policyStatus("ACTIVE")
                .ruleCatalogVersion("RC-2.0.0-proposed.2")
                .ruleCatalogStatus("ACTIVE")
                .evaluationMode("ACTIVE_RUNTIME")
                .recommendationOnly(true)
                .candidateRules(List.of())
                .build();
        when(adminReportAiResolutionService.getPolicy(reportId)).thenReturn(response);

        AdminReportAiPolicyResponseDTO result = adminContentReportController.getAiPolicy(reportId);

        assertThat(result).isSameAs(response);
        assertThat(result.getReportId()).isEqualTo(reportId);
        assertThat(result.getRecommendationOnly()).isTrue();
        assertThat(result.getEvaluationMode()).isEqualTo("ACTIVE_RUNTIME");
        verify(adminReportAiResolutionService).getPolicy(reportId);
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

    private AdminReportAiAutoApplyJobResponseDTO autoApplyJobResponse(UUID reportId) {
        AdminReportAiAutoApplyJobResponseDTO response = new AdminReportAiAutoApplyJobResponseDTO();
        response.setId(UUID.randomUUID());
        response.setContentReportId(reportId);
        response.setAiResolutionId(UUID.randomUUID());
        response.setStatus(AdminReportAiAutoApplyJobStatus.SCHEDULED);
        response.setReportDecision(AdminReportAiReportDecision.RESOLVE);
        response.setTargetAction(AdminReportAiTargetAction.HIDE);
        response.setTargetType(ReportTargetType.BLOG);
        response.setTargetId(UUID.randomUUID());
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "admin", List.of("ADMIN"));
    }
}
