package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.Set;

@Data
public class AdminReportAiResolutionCreateRequestDTO {

    private static final Set<Integer> ALLOWED_AUTO_APPLY_DELAYS = Set.of(5, 15, 30, 60, 120, 360, 720);

    private Boolean autoApplyEnabled = false;

    private Integer autoApplyDelayMinutes;

    public boolean isAutoApplyEnabled() {
        return Boolean.TRUE.equals(autoApplyEnabled);
    }

    @AssertTrue(message = "autoApplyDelayMinutes must be blank or one of 5, 15, 30, 60, 120, 360, 720")
    public boolean isAutoApplyDelayValid() {
        if (!isAutoApplyEnabled()) {
            return true;
        }
        return autoApplyDelayMinutes == null || ALLOWED_AUTO_APPLY_DELAYS.contains(autoApplyDelayMinutes);
    }
}
