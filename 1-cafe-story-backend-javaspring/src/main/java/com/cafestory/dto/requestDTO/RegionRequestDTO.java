package com.cafestory.dto.requestDTO;

import lombok.Data;

@Data
public class RegionRequestDTO {
    private String cityCode;
    private String city;
    private String provinceCode;
    private String province;
    private String wardCode;
    private String district;
    private String ward;
    private String area;
    private String street;
}
