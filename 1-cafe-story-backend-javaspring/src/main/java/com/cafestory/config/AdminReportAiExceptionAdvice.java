package com.cafestory.config;

import com.cafestory.dto.responseDTO.AdminReportAiOperationalErrorResponseDTO;
import com.cafestory.exception.AdminReportAiProviderBoundaryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminReportAiExceptionAdvice {

    @ExceptionHandler(AdminReportAiProviderBoundaryException.class)
    public ResponseEntity<AdminReportAiOperationalErrorResponseDTO> handleProviderBoundaryFailure(
            AdminReportAiProviderBoundaryException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new AdminReportAiOperationalErrorResponseDTO(exception.getCorrelationId()));
    }
}
