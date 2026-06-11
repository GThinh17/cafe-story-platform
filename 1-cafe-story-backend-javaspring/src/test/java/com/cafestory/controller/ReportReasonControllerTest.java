package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ReportReasonResponseDTO;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.ReportReasonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportReasonControllerTest {

    @Mock
    private ReportReasonService reportReasonService;

    @InjectMocks
    private ReportReasonController reportReasonController;

    @Test
    void getActiveReportReasons_success_TC001() {
        ReportReasonResponseDTO response = new ReportReasonResponseDTO();
        response.setCode("SCAM_FRAUD_OR_SPAM");
        response.setSeverity(4);

        when(reportReasonService.getActiveReportReasons(ReportTargetType.BLOG)).thenReturn(List.of(response));

        List<ReportReasonResponseDTO> result = reportReasonController.getActiveReportReasons(ReportTargetType.BLOG);

        assertThat(result).containsExactly(response);
        verify(reportReasonService).getActiveReportReasons(ReportTargetType.BLOG);
    }
}
