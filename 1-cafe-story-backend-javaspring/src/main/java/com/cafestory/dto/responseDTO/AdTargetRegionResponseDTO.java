package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class AdTargetRegionResponseDTO {
    private UUID adTargetRegionId;
    private String province;
    private String city;
    private String area;
    private String ward;
}
