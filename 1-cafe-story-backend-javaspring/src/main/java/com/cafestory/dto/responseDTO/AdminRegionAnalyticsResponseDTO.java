package com.cafestory.dto.responseDTO;

import lombok.Data;

@Data
public class AdminRegionAnalyticsResponseDTO {

    private String provinceCode;
    private String provinceName;
    private long userCount;
    private long cafePageCount;
    private long reviewerCount;
}
