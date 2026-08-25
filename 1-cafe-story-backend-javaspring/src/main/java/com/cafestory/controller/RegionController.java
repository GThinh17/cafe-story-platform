package com.cafestory.controller;

import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.responseDTO.RegionCityResponseDTO;
import com.cafestory.dto.responseDTO.RegionProvinceResponseDTO;
import com.cafestory.dto.responseDTO.RegionResponseDTO;
import com.cafestory.dto.responseDTO.RegionWardResponseDTO;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.service.serviceInterface.RegionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping("/provinces")
    public List<RegionProvinceResponseDTO> getProvinces() {
        return regionService.getProvinces();
    }

    @GetMapping("/cities")
    public List<RegionCityResponseDTO> getCities(@RequestParam(required = false) String provinceCode) {
        return regionService.getCities(provinceCode);
    }

    @GetMapping("/wards")
    public List<RegionWardResponseDTO> getWards(
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String cityCode) {
        return regionService.getWards(provinceCode, cityCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegionResponseDTO createRegion(
            @RequestBody RegionRequestDTO request,
            @RequestParam(defaultValue = "FULL_ADDRESS") RegionRequirement requirement) {
        return regionService.createRegion(request, requirement);
    }
}
