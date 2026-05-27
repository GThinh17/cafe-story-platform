package com.cafestory.controller;

import com.cafestory.dto.requestDTO.ContentReportRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentReportControllerTest {

    @Mock
    private ContentReportService contentReportService;

    @InjectMocks
    private ContentReportController contentReportController;

    @Test
    void createReport_success_TC001() {
        UUID userId = UUID.randomUUID();
        ContentReportRequestDTO request = new ContentReportRequestDTO();
        request.setTargetType(ReportTargetType.BLOG);
        request.setTargetId(UUID.randomUUID());
        request.setReason("SPAM");
        ContentReportResponseDTO response = new ContentReportResponseDTO();
        response.setId(UUID.randomUUID());

        when(contentReportService.createReport(userId, request)).thenReturn(response);

        ContentReportResponseDTO result = contentReportController.createReport(principal(userId), request);

        assertThat(result).isEqualTo(response);
        verify(contentReportService).createReport(userId, request);
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
