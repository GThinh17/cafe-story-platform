package com.cafestory.service;

import com.cafestory.dto.responseDTO.ReportReasonResponseDTO;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.ReportReasonRepository;
import com.cafestory.service.serviceImplement.ReportReasonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportReasonServiceImplTest {

    @Mock
    private ReportReasonRepository reportReasonRepository;

    private ReportReasonServiceImpl reportReasonService;

    @BeforeEach
    void setUp() {
        reportReasonService = new ReportReasonServiceImpl(reportReasonRepository);
    }

    @Test
    void getActiveReportReasons_success_returnsActiveReasonsSortedBySeverity_TC001() {
        ReportReason severe = reason("VIOLENCE_HATE_OR_EXPLOITATION", "Bạo lực, thù ghét hoặc bóc lột", 5);
        ReportReason spam = reason("SCAM_FRAUD_OR_SPAM", "Lừa đảo, gian lận hoặc spam", 4);

        when(reportReasonRepository.findByActiveTrueOrderBySeverityDescSortOrderAscLabelViAsc())
                .thenReturn(List.of(severe, spam));

        List<ReportReasonResponseDTO> result = reportReasonService.getActiveReportReasons(null);

        assertThat(result).extracting(ReportReasonResponseDTO::getCode)
                .containsExactly("VIOLENCE_HATE_OR_EXPLOITATION", "SCAM_FRAUD_OR_SPAM");
        assertThat(result.getFirst().getSeverity()).isEqualTo(5);
        assertThat(result.getFirst().getIsActive()).isTrue();
    }

    @Test
    void getActiveReportReasons_success_filtersByTargetTypeWithSharedReasons_TC002() {
        ReportReason blogReason = reason("FALSE_INFORMATION", "Thông tin sai sự thật", 3);
        blogReason.setTargetType(ReportTargetType.BLOG);

        when(reportReasonRepository.findActiveReasonsForTargetType(ReportTargetType.BLOG))
                .thenReturn(List.of(blogReason));

        List<ReportReasonResponseDTO> result = reportReasonService.getActiveReportReasons(ReportTargetType.BLOG);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTargetType()).isEqualTo(ReportTargetType.BLOG);
        verify(reportReasonRepository).findActiveReasonsForTargetType(ReportTargetType.BLOG);
    }

    @Test
    void validateActiveReportReason_fail_inactiveReason_TC003() {
        UUID reasonId = UUID.randomUUID();
        ReportReason reason = reason("SCAM_FRAUD_OR_SPAM", "Lừa đảo, gian lận hoặc spam", 4);
        reason.setId(reasonId);
        reason.setActive(false);

        when(reportReasonRepository.findById(reasonId)).thenReturn(Optional.of(reason));

        assertThatThrownBy(() -> reportReasonService.validateActiveReportReason(reasonId, ReportTargetType.BLOG))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report reason is inactive"));
    }

    @Test
    void validateActiveReportReason_fail_targetTypeMismatch_TC004() {
        UUID reasonId = UUID.randomUUID();
        ReportReason reason = reason("FALSE_INFORMATION", "Thông tin sai sự thật", 3);
        reason.setId(reasonId);
        reason.setTargetType(ReportTargetType.BLOG);

        when(reportReasonRepository.findById(reasonId)).thenReturn(Optional.of(reason));

        assertThatThrownBy(() -> reportReasonService.validateActiveReportReason(reasonId, ReportTargetType.USER))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report reason does not match target type"));
    }

    private ReportReason reason(String code, String labelVi, int severity) {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode(code);
        reason.setLabelVi(labelVi);
        reason.setSeverity(severity);
        reason.setActive(true);
        reason.setRequiresDescription(false);
        reason.setSortOrder(10);
        return reason;
    }
}
