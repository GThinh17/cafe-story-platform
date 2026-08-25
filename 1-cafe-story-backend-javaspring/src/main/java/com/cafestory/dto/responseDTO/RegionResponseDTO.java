package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class RegionResponseDTO {
    private UUID regionId;
    private String cityCode;
    private String city;
    private String provinceCode;
    private String province;
    private String wardCode;
    private String ward;
    private String area;
    private String street;
}
