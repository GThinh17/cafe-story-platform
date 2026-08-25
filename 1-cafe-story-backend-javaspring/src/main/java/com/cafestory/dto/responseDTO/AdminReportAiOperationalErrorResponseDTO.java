package com.cafestory.dto.responseDTO;

import com.cafestory.exception.AdminReportAiProviderBoundaryException;
import com.cafestory.until.FormatResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class AdminReportAiOperationalErrorResponseDTO extends FormatResponse<Object> {

    private final String code;
    private final UUID correlationId;
    private final boolean retryable;
    private final String stage;

    public AdminReportAiOperationalErrorResponseDTO(UUID correlationId) {
        super(
                HttpStatus.BAD_GATEWAY.value(),
                "Fail",
                AdminReportAiProviderBoundaryException.SAFE_MESSAGE,
                null);
        this.code = AdminReportAiProviderBoundaryException.CODE;
        this.correlationId = correlationId;
        this.retryable = true;
        this.stage = AdminReportAiProviderBoundaryException.STAGE;
    }
}
