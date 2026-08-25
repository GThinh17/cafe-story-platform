package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.responseDTO.RegionCityResponseDTO;
import com.cafestory.dto.responseDTO.RegionProvinceResponseDTO;
import com.cafestory.dto.responseDTO.RegionResponseDTO;
import com.cafestory.dto.responseDTO.RegionWardResponseDTO;
import com.cafestory.entity.Region;
import com.cafestory.entity.enums.RegionRequirement;

import java.util.List;
import java.util.UUID;

public interface RegionService {

    RegionResponseDTO createRegion(RegionRequestDTO request, RegionRequirement requirement);

    List<RegionProvinceResponseDTO> getProvinces();

    List<RegionCityResponseDTO> getCities(String provinceCode);

    List<RegionWardResponseDTO> getWards(String provinceCode, String cityCode);

    Region resolveExistingRegion(UUID regionId, RegionRequirement requirement);

    Region upsertRegion(Region currentRegion, RegionRequestDTO request, RegionRequirement requirement);
}
