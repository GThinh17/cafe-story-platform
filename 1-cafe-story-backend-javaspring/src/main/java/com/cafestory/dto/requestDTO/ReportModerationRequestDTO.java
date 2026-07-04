package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportModerationRequestDTO {

    private UUID reportId;

    private ReportTargetType targetType;

    private UUID targetId;

    private String reasonCode;

    private String reasonLabel;

    private String description;

    private String contentText;

    private List<String> imageUrls;
}
