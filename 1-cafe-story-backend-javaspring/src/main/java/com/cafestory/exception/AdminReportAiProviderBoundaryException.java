package com.cafestory.exception;

import java.util.UUID;

public class AdminReportAiProviderBoundaryException extends RuntimeException {

    public static final String CODE = "AI_PROVIDER_BOUNDARY_FAILED";
    public static final String SAFE_MESSAGE = "AI recommendation service is unavailable.";
    public static final String STAGE = "N8N_PROVIDER";

    private final UUID correlationId;

    public AdminReportAiProviderBoundaryException(UUID correlationId, Throwable cause) {
        super(SAFE_MESSAGE, cause);
        this.correlationId = correlationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }
}
