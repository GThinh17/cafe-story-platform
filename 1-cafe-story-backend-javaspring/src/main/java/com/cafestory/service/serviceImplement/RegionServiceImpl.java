package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.responseDTO.RegionCityResponseDTO;
import com.cafestory.dto.responseDTO.RegionProvinceResponseDTO;
import com.cafestory.dto.responseDTO.RegionResponseDTO;
import com.cafestory.dto.responseDTO.RegionWardResponseDTO;
import com.cafestory.entity.Region;
import com.cafestory.entity.RegionCity;
import com.cafestory.entity.RegionProvince;
import com.cafestory.entity.RegionWard;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.repository.RegionCityRepository;
import com.cafestory.repository.RegionProvinceRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.RegionWardRepository;
import com.cafestory.service.serviceInterface.RegionService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final RegionProvinceRepository provinceRepository;
    private final RegionCityRepository cityRepository;
    private final RegionWardRepository wardRepository;

    public RegionServiceImpl(
            RegionRepository regionRepository,
            RegionProvinceRepository provinceRepository,
            RegionCityRepository cityRepository,
            RegionWardRepository wardRepository) {
        this.regionRepository = regionRepository;
        this.provinceRepository = provinceRepository;
        this.cityRepository = cityRepository;
        this.wardRepository = wardRepository;
    }

    @Override
    @Transactional
    public RegionResponseDTO createRegion(RegionRequestDTO request, RegionRequirement requirement) {
        return toRegionResponseDTO(upsertRegion(new Region(), request, requirement));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.REGION_PROVINCES_CACHE, key = "'all'")
    public List<RegionProvinceResponseDTO> getProvinces() {
        return provinceRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toProvinceResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.REGION_CITIES_CACHE, key = "#p0 == null ? 'all' : #p0.trim()")
    public List<RegionCityResponseDTO> getCities(String provinceCode) {
        List<RegionCity> cities = hasText(provinceCode)
                ? cityRepository.findByProvinceProvinceCodeOrderByNameAsc(provinceCode.trim())
                : cityRepository.findAll();

        return cities.stream()
                .map(this::toCityResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.REGION_WARDS_CACHE,
            key = "(#p0 == null ? 'all' : #p0.trim()) + ':' + (#p1 == null ? 'all' : #p1.trim())")
    public List<RegionWardResponseDTO> getWards(String provinceCode, String cityCode) {
        List<RegionWard> wards;
        if (hasText(cityCode)) {
            wards = wardRepository.findByCityCityCodeOrderByNameAsc(cityCode.trim());
        } else if (hasText(provinceCode)) {
            wards = wardRepository.findByProvinceProvinceCodeOrderByNameAsc(provinceCode.trim());
        } else {
            wards = wardRepository.findAll();
        }

        return wards.stream()
                .map(this::toWardResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Region resolveExistingRegion(UUID regionId, RegionRequirement requirement) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found"));
        validateRegion(region, requirement);
        return region;
    }

    @Override
    @Transactional
    public Region upsertRegion(Region currentRegion, RegionRequestDTO request, RegionRequirement requirement) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region is required");
        }

        Region region = currentRegion == null ? new Region() : currentRegion;
        RegionProvince province = resolveProvince(request);
        RegionCity city = resolveCity(request, province);
        RegionWard ward = requirement == RegionRequirement.FULL_ADDRESS
                ? resolveWard(request, province, city)
                : resolveOptionalWard(request, province);
        if (ward != null) {
            validateWardInRegion(ward, province, city);
        }
        String street = normalize(request.getStreet());
        String area = firstNonBlank(request.getArea(), request.getDistrict());

        if (requirement == RegionRequirement.FULL_ADDRESS && !hasText(street)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Street is required");
        }

        region.setProvinceRef(province);
        region.setCityRef(city);
        region.setWardRef(ward);
        region.setProvince(province.getName());
        region.setCity(city.getName());
        region.setWard(ward == null ? null : ward.getName());
        region.setArea(area);
        region.setStreet(street);

        validateRegion(region, requirement);
        return regionRepository.save(region);
    }

    private RegionProvince resolveProvince(RegionRequestDTO request) {
        String provinceCode = normalize(request.getProvinceCode());
        if (hasText(provinceCode)) {
            return provinceRepository.findById(provinceCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Province not found"));
        }

        String provinceName = normalize(request.getProvince());
        if (hasText(provinceName)) {
            return provinceRepository.findByNameIgnoreCase(provinceName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Province not found"));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Province is required");
    }

    private RegionCity resolveCity(RegionRequestDTO request, RegionProvince province) {
        String cityCode = normalize(request.getCityCode());
        if (hasText(cityCode)) {
            RegionCity city = cityRepository.findById(cityCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
            validateCityInProvince(city, province);
            return city;
        }

        String cityName = normalize(request.getCity());
        if (hasText(cityName)) {
            return cityRepository.findByProvinceProvinceCodeAndNameIgnoreCase(province.getProvinceCode(), cityName)
                    .or(() -> cityRepository.findByNameIgnoreCase(cityName))
                    .filter(city -> province.getProvinceCode().equals(city.getProvince().getProvinceCode()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City is required");
    }

    private RegionWard resolveWard(RegionRequestDTO request, RegionProvince province, RegionCity city) {
        RegionWard ward = resolveOptionalWard(request, province);
        if (ward == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ward is required");
        }
        validateWardInRegion(ward, province, city);
        return ward;
    }

    private RegionWard resolveOptionalWard(RegionRequestDTO request, RegionProvince province) {
        String wardCode = normalize(request.getWardCode());
        if (hasText(wardCode)) {
            return wardRepository.findById(wardCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ward not found"));
        }

        String wardName = normalize(request.getWard());
        if (hasText(wardName)) {
            return wardRepository.findFirstByProvinceProvinceCodeAndNameIgnoreCase(
                    province.getProvinceCode(),
                    wardName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ward not found"));
        }

        return null;
    }

    private void validateRegion(Region region, RegionRequirement requirement) {
        if (!hasText(region.getProvince()) || !hasText(region.getCity())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Province and city are required");
        }

        if (requirement == RegionRequirement.FULL_ADDRESS
                && (!hasText(region.getWard()) || !hasText(region.getStreet()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ward and street are required");
        }
    }

    private void validateCityInProvince(RegionCity city, RegionProvince province) {
        if (!province.getProvinceCode().equals(city.getProvince().getProvinceCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City does not belong to province");
        }
    }

    private void validateWardInRegion(RegionWard ward, RegionProvince province, RegionCity city) {
        if (!province.getProvinceCode().equals(ward.getProvince().getProvinceCode())
                || !city.getCityCode().equals(ward.getCity().getCityCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ward does not belong to selected region");
        }
    }

    private RegionProvinceResponseDTO toProvinceResponseDTO(RegionProvince province) {
        RegionProvinceResponseDTO response = new RegionProvinceResponseDTO();
        response.setProvinceCode(province.getProvinceCode());
        response.setName(province.getName());
        return response;
    }

    private RegionCityResponseDTO toCityResponseDTO(RegionCity city) {
        RegionCityResponseDTO response = new RegionCityResponseDTO();
        response.setCityCode(city.getCityCode());
        response.setProvinceCode(city.getProvince().getProvinceCode());
        response.setName(city.getName());
        return response;
    }

    private RegionWardResponseDTO toWardResponseDTO(RegionWard ward) {
        RegionWardResponseDTO response = new RegionWardResponseDTO();
        response.setWardCode(ward.getWardCode());
        response.setCityCode(ward.getCity().getCityCode());
        response.setProvinceCode(ward.getProvince().getProvinceCode());
        response.setName(ward.getName());
        return response;
    }

    private RegionResponseDTO toRegionResponseDTO(Region region) {
        RegionResponseDTO response = new RegionResponseDTO();
        response.setRegionId(region.getRegionId());
        response.setProvinceCode(region.getProvinceRef() == null ? null : region.getProvinceRef().getProvinceCode());
        response.setProvince(region.getProvince());
        response.setCityCode(region.getCityRef() == null ? null : region.getCityRef().getCityCode());
        response.setCity(region.getCity());
        response.setWardCode(region.getWardRef() == null ? null : region.getWardRef().getWardCode());
        response.setWard(region.getWard());
        response.setArea(region.getArea());
        response.setStreet(region.getStreet());
        return response;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first.trim() : normalize(second);
    }
}
