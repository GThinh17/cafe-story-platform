package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ModerationDecision;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReportModerationResponseDTO {

    private ModerationDecision decision;

    private Double score;

    private List<String> labels;

    private String explanation;

    private String modelName;

    private Map<String, Object> rawCategories;
}
