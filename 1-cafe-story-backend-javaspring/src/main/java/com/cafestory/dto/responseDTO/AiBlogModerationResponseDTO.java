package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AiBlogModerationResponseDTO {

    private UUID blogId;

    private Integer captionScore;

    private String captionReason;

    private Integer imageScore;

    private String imageReason;

    private List<String> tags;

    private String status;
}
