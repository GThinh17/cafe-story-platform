package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
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

    private ContentReportResponseDTO response() {
        ContentReportResponseDTO response = new ContentReportResponseDTO();
        response.setId(UUID.randomUUID());
        response.setStatus(ReportStatus.OPEN);
        return response;
    }
}
