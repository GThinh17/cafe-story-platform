package com.cafestory.dto.requestDTO;

import lombok.Data;

@Data
public class AdTargetRegionRequestDTO {
    private String province;
    private String city;
    private String area;
    private String ward;
}
