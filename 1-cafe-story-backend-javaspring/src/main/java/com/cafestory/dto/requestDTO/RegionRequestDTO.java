package com.cafestory.dto.requestDTO;

import lombok.Data;

@Data
public class RegionRequestDTO {
    private String city;
    private String province;
    private String district;
    private String ward;
    private String area;
    private String street;
}
