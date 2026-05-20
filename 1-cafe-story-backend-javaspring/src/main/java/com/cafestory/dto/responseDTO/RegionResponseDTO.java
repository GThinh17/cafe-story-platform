package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class RegionResponseDTO {
    private UUID regionId;
    private String city;
    private String province;
    private String ward;
    private String area;
    private String street;
}
