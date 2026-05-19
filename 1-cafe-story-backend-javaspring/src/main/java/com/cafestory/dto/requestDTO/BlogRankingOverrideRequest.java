package com.cafestory.dto.requestDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogRankingOverrideRequest {

    @JsonProperty("boost_score")
    private Double boostScore;

    @JsonProperty("is_pinned")
    private Boolean isPinned;

    private String reason;

    @JsonProperty("start_at")
    @NotNull(message = "Start at is mandatory")
    private LocalDateTime startAt;

    @JsonProperty("end_at")
    private LocalDateTime endAt;

    @JsonProperty("created_by")
    private UUID createdBy;
}
