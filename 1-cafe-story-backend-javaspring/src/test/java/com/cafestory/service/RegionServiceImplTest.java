package com.cafestory.service;

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
import com.cafestory.service.serviceImplement.RegionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link RegionServiceImpl}.
 *
 * <p>Trọng tâm là cây phân giải tỉnh → thành phố → phường: mỗi cấp nhận được cả
 * mã lẫn tên, mỗi cấp có nhánh không tìm thấy, và hai mức ràng buộc
 * {@link RegionRequirement} cho ra hai bộ quy tắc bắt buộc khác nhau.
 */
@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

    @Mock
    private RegionRepository regionRepository;
    @Mock
    private RegionProvinceRepository provinceRepository;
    @Mock
    private RegionCityRepository cityRepository;
    @Mock
    private RegionWardRepository wardRepository;

    private RegionServiceImpl regionService;

    private RegionProvince province;
    private RegionCity city;
    private RegionWard ward;

    @BeforeEach
    void setUp() {
        regionService = new RegionServiceImpl(
                regionRepository, provinceRepository, cityRepository, wardRepository);

        province = province("92", "Can Tho");
        city = city("92-01", "Ninh Kieu", province);
        ward = ward("92-01-01", "An Khanh", province, city);
    }

    // ------------------------------------------------------------ Danh mục

    @Test
    void getProvinces_success_mapsSortedProvinces_TC001() {
        when(provinceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(province, province("79", "Ho Chi Minh")));

        List<RegionProvinceResponseDTO> result = regionService.getProvinces();

        assertThat(result).extracting(RegionProvinceResponseDTO::getProvinceCode)
                .containsExactly("92", "79");
        assertThat(result.get(0).getName()).isEqualTo("Can Tho");
    }

    @Test
    void getCities_success_filtersByProvinceCode_TC002() {
        when(cityRepository.findByProvinceProvinceCodeOrderByNameAsc("92")).thenReturn(List.of(city));

        List<RegionCityResponseDTO> result = regionService.getCities("  92  ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCityCode()).isEqualTo("92-01");
        assertThat(result.get(0).getProvinceCode()).isEqualTo("92");
        assertThat(result.get(0).getName()).isEqualTo("Ninh Kieu");
    }

    @Test
    void getCities_success_blankProvinceCodeReturnsAll_TC003() {
        when(cityRepository.findAll()).thenReturn(List.of(city));

        assertThat(regionService.getCities(null)).hasSize(1);
        assertThat(regionService.getCities("   ")).hasSize(1);
        verify(cityRepository, never()).findByProvinceProvinceCodeOrderByNameAsc(any());
    }

    @Test
    void getWards_success_prefersCityCodeOverProvinceCode_TC004() {
        when(wardRepository.findByCityCityCodeOrderByNameAsc("92-01")).thenReturn(List.of(ward));

        List<RegionWardResponseDTO> result = regionService.getWards("92", " 92-01 ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWardCode()).isEqualTo("92-01-01");
        assertThat(result.get(0).getCityCode()).isEqualTo("92-01");
        assertThat(result.get(0).getProvinceCode()).isEqualTo("92");
        verify(wardRepository, never()).findByProvinceProvinceCodeOrderByNameAsc(any());
    }

    @Test
    void getWards_success_fallsBackToProvinceCode_TC005() {
        when(wardRepository.findByProvinceProvinceCodeOrderByNameAsc("92")).thenReturn(List.of(ward));

        assertThat(regionService.getWards(" 92 ", "  ")).hasSize(1);
    }

    @Test
    void getWards_success_noFilterReturnsAll_TC006() {
        when(wardRepository.findAll()).thenReturn(List.of(ward));

        assertThat(regionService.getWards(null, null)).hasSize(1);
    }

    // ----------------------------------------------------- resolveExisting

    @Test
    void resolveExistingRegion_success_returnsValidRegion_TC007() {
        Region region = region();
        UUID regionId = region.getRegionId();
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));

        assertThat(regionService.resolveExistingRegion(regionId, RegionRequirement.FULL_ADDRESS))
                .isSameAs(region);
    }

    @Test
    void resolveExistingRegion_fail_notFound_TC008() {
        UUID regionId = UUID.randomUUID();
        when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.resolveExistingRegion(regionId, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Region not found");
    }

    @Test
    void resolveExistingRegion_fail_missingWardForFullAddress_TC009() {
        Region region = region();
        region.setWard(null);
        UUID regionId = region.getRegionId();
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> regionService.resolveExistingRegion(regionId, RegionRequirement.FULL_ADDRESS))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ward and street are required");
    }

    @Test
    void resolveExistingRegion_fail_missingProvinceOrCity_TC010() {
        Region region = region();
        region.setCity("  ");
        UUID regionId = region.getRegionId();
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> regionService.resolveExistingRegion(regionId, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Province and city are required");
    }

    // ------------------------------------------------------ createRegion

    @Test
    void createRegion_success_resolvesEverythingByCode_TC011() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode(" 92 ");
        request.setCityCode("92-01");
        request.setWardCode("92-01-01");
        request.setStreet(" 30/4 ");
        request.setArea(" Ninh Kieu ");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findById("92-01-01")).thenReturn(Optional.of(ward));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> {
            Region saved = invocation.getArgument(0);
            saved.setRegionId(UUID.randomUUID());
            return saved;
        });

        RegionResponseDTO result = regionService.createRegion(request, RegionRequirement.FULL_ADDRESS);

        assertThat(result.getProvinceCode()).isEqualTo("92");
        assertThat(result.getProvince()).isEqualTo("Can Tho");
        assertThat(result.getCityCode()).isEqualTo("92-01");
        assertThat(result.getCity()).isEqualTo("Ninh Kieu");
        assertThat(result.getWardCode()).isEqualTo("92-01-01");
        assertThat(result.getWard()).isEqualTo("An Khanh");
        assertThat(result.getStreet()).isEqualTo("30/4");
        assertThat(result.getArea()).isEqualTo("Ninh Kieu");
    }

    @Test
    void createRegion_success_blogLocationWithoutWardOrStreet_TC012() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvince("can tho");
        request.setCity("ninh kieu");
        request.setDistrict(" Quan Ninh Kieu ");
        when(provinceRepository.findByNameIgnoreCase("can tho")).thenReturn(Optional.of(province));
        when(cityRepository.findByProvinceProvinceCodeAndNameIgnoreCase("92", "ninh kieu"))
                .thenReturn(Optional.of(city));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegionResponseDTO result = regionService.createRegion(request, RegionRequirement.BLOG_LOCATION);

        assertThat(result.getWard()).isNull();
        assertThat(result.getWardCode()).isNull();
        assertThat(result.getStreet()).isNull();
        // area rỗng thì rơi về district, và district được chuẩn hoá khoảng trắng.
        assertThat(result.getArea()).isEqualTo("Quan Ninh Kieu");
    }

    @Test
    void upsertRegion_success_reusesCurrentRegionInstance_TC013() {
        Region current = region();
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(regionRepository.save(current)).thenReturn(current);

        Region result = regionService.upsertRegion(current, request, RegionRequirement.BLOG_LOCATION);

        assertThat(result).isSameAs(current);
        assertThat(result.getWard()).isNull();
        assertThat(result.getProvinceRef()).isSameAs(province);
        assertThat(result.getCityRef()).isSameAs(city);
    }

    @Test
    void upsertRegion_success_nullCurrentRegionCreatesNew_TC014() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Region result = regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION);

        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Ninh Kieu");
    }

    @Test
    void upsertRegion_fail_nullRequest_TC015() {
        assertThatThrownBy(() -> regionService.upsertRegion(new Region(), null, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Region is required");
    }

    // --------------------------------------------------- Nhánh lỗi phân giải

    @Test
    void upsertRegion_fail_provinceCodeNotFound_TC016() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("00");
        when(provinceRepository.findById("00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Province not found");
    }

    @Test
    void upsertRegion_fail_provinceNameNotFound_TC017() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvince("Khong Co");
        when(provinceRepository.findByNameIgnoreCase("Khong Co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Province not found");
    }

    @Test
    void upsertRegion_fail_provinceMissing_TC018() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setCityCode("92-01");

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Province is required");
    }

    @Test
    void upsertRegion_fail_cityCodeNotFound_TC019() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("00-00");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("00-00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("City not found");
    }

    @Test
    void upsertRegion_fail_cityBelongsToAnotherProvince_TC020() {
        RegionProvince otherProvince = province("79", "Ho Chi Minh");
        RegionCity foreignCity = city("79-01", "Quan 1", otherProvince);
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("79-01");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("79-01")).thenReturn(Optional.of(foreignCity));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("City does not belong to province");
    }

    @Test
    void upsertRegion_success_cityNameFallsBackToGlobalLookup_TC021() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCity("ninh kieu");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findByProvinceProvinceCodeAndNameIgnoreCase("92", "ninh kieu"))
                .thenReturn(Optional.empty());
        when(cityRepository.findByNameIgnoreCase("ninh kieu")).thenReturn(Optional.of(city));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Region result = regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION);

        assertThat(result.getCity()).isEqualTo("Ninh Kieu");
    }

    @Test
    void upsertRegion_fail_cityNameResolvesOutsideProvince_TC022() {
        RegionProvince otherProvince = province("79", "Ho Chi Minh");
        RegionCity foreignCity = city("79-01", "Quan 1", otherProvince);
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCity("quan 1");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findByProvinceProvinceCodeAndNameIgnoreCase("92", "quan 1"))
                .thenReturn(Optional.empty());
        when(cityRepository.findByNameIgnoreCase("quan 1")).thenReturn(Optional.of(foreignCity));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("City not found");
    }

    @Test
    void upsertRegion_fail_cityMissing_TC023() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("City is required");
    }

    @Test
    void upsertRegion_fail_wardCodeNotFound_TC024() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setWardCode("00-00-00");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findById("00-00-00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ward not found");
    }

    @Test
    void upsertRegion_success_wardResolvedByName_TC025() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setWard("an khanh");
        request.setStreet("30/4");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findFirstByProvinceProvinceCodeAndNameIgnoreCase("92", "an khanh"))
                .thenReturn(Optional.of(ward));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Region result = regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS);

        assertThat(result.getWard()).isEqualTo("An Khanh");
        assertThat(result.getStreet()).isEqualTo("30/4");
    }

    @Test
    void upsertRegion_fail_wardNameNotFound_TC026() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setWard("khong co");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findFirstByProvinceProvinceCodeAndNameIgnoreCase("92", "khong co"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ward not found");
    }

    @Test
    void upsertRegion_fail_wardRequiredForFullAddress_TC027() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setStreet("30/4");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ward is required");
    }

    @Test
    void upsertRegion_fail_streetRequiredForFullAddress_TC028() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setWardCode("92-01-01");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findById("92-01-01")).thenReturn(Optional.of(ward));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Street is required");
    }

    @Test
    void upsertRegion_fail_wardBelongsToAnotherProvince_TC029() {
        RegionProvince otherProvince = province("79", "Ho Chi Minh");
        RegionWard foreignWard = ward("79-01-01", "Ben Nghe", otherProvince, city("79-01", "Quan 1", otherProvince));
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setWardCode("79-01-01");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findById("79-01-01")).thenReturn(Optional.of(foreignWard));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.BLOG_LOCATION))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ward does not belong to selected region");
    }

    @Test
    void upsertRegion_fail_wardBelongsToAnotherCity_TC030() {
        RegionCity otherCity = city("92-02", "O Mon", province);
        RegionWard foreignWard = ward("92-02-01", "Chau Van Liem", province, otherCity);
        RegionRequestDTO request = new RegionRequestDTO();
        request.setProvinceCode("92");
        request.setCityCode("92-01");
        request.setWardCode("92-02-01");
        request.setStreet("30/4");
        when(provinceRepository.findById("92")).thenReturn(Optional.of(province));
        when(cityRepository.findById("92-01")).thenReturn(Optional.of(city));
        when(wardRepository.findById("92-02-01")).thenReturn(Optional.of(foreignWard));

        assertThatThrownBy(() -> regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ------------------------------------------------------------- Helpers

    private RegionProvince province(String code, String name) {
        RegionProvince newProvince = new RegionProvince();
        newProvince.setProvinceCode(code);
        newProvince.setName(name);
        return newProvince;
    }

    private RegionCity city(String code, String name, RegionProvince owner) {
        RegionCity newCity = new RegionCity();
        newCity.setCityCode(code);
        newCity.setName(name);
        newCity.setProvince(owner);
        return newCity;
    }

    private RegionWard ward(String code, String name, RegionProvince owner, RegionCity owningCity) {
        RegionWard newWard = new RegionWard();
        newWard.setWardCode(code);
        newWard.setName(name);
        newWard.setProvince(owner);
        newWard.setCity(owningCity);
        return newWard;
    }

    private Region region() {
        Region newRegion = new Region();
        newRegion.setRegionId(UUID.randomUUID());
        newRegion.setProvinceRef(province);
        newRegion.setCityRef(city);
        newRegion.setWardRef(ward);
        newRegion.setProvince("Can Tho");
        newRegion.setCity("Ninh Kieu");
        newRegion.setWard("An Khanh");
        newRegion.setArea("Ninh Kieu");
        newRegion.setStreet("30/4");
        return newRegion;
    }
}
