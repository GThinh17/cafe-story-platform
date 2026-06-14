package com.cafestory.service;

import com.cafestory.config.CacheConfig;
import com.cafestory.entity.RegionCity;
import com.cafestory.entity.RegionProvince;
import com.cafestory.entity.RegionWard;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.RegionCityRepository;
import com.cafestory.repository.RegionProvinceRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.RegionWardRepository;
import com.cafestory.repository.ReportReasonRepository;
import com.cafestory.service.serviceImplement.RegionServiceImpl;
import com.cafestory.service.serviceImplement.ReportReasonServiceImpl;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.service.serviceInterface.ReportReasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = CacheableServiceTest.TestCacheConfig.class)
class CacheableServiceTest {

    @jakarta.annotation.Resource
    private RegionService regionService;

    @jakarta.annotation.Resource
    private ReportReasonService reportReasonService;

    @jakarta.annotation.Resource
    private RegionProvinceRepository provinceRepository;

    @jakarta.annotation.Resource
    private RegionCityRepository cityRepository;

    @jakarta.annotation.Resource
    private RegionWardRepository wardRepository;

    @jakarta.annotation.Resource
    private ReportReasonRepository reportReasonRepository;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(provinceRepository, cityRepository, wardRepository, reportReasonRepository);
        clearCache(CacheConfig.REGION_PROVINCES_CACHE);
        clearCache(CacheConfig.REGION_CITIES_CACHE);
        clearCache(CacheConfig.REGION_WARDS_CACHE);
        clearCache(CacheConfig.REPORT_REASONS_CACHE);
    }

    @Test
    void getProvinces_success_usesCacheForRepeatedCalls_TC001() {
        when(provinceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(province("79", "Ho Chi Minh")));

        assertThat(regionService.getProvinces()).hasSize(1);
        assertThat(regionService.getProvinces()).hasSize(1);

        verify(provinceRepository, times(1)).findAllByOrderByNameAsc();
    }

    @Test
    void getCities_success_usesDifferentCacheKeysPerProvince_TC002() {
        when(cityRepository.findByProvinceProvinceCodeOrderByNameAsc("79"))
                .thenReturn(List.of(city("79-1", "79", "Ho Chi Minh")));
        when(cityRepository.findByProvinceProvinceCodeOrderByNameAsc("01"))
                .thenReturn(List.of(city("01-1", "01", "Ha Noi")));

        assertThat(regionService.getCities("79")).extracting("cityCode").containsExactly("79-1");
        assertThat(regionService.getCities("79")).extracting("cityCode").containsExactly("79-1");
        assertThat(regionService.getCities("01")).extracting("cityCode").containsExactly("01-1");

        verify(cityRepository, times(1)).findByProvinceProvinceCodeOrderByNameAsc("79");
        verify(cityRepository, times(1)).findByProvinceProvinceCodeOrderByNameAsc("01");
    }

    @Test
    void getWards_success_usesDifferentCacheKeysPerCity_TC003() {
        when(wardRepository.findByCityCityCodeOrderByNameAsc("79-1"))
                .thenReturn(List.of(ward("26734", "79", "79-1", "Ben Nghe")));
        when(wardRepository.findByCityCityCodeOrderByNameAsc("01-1"))
                .thenReturn(List.of(ward("00001", "01", "01-1", "Phuc Xa")));

        assertThat(regionService.getWards("79", "79-1")).extracting("wardCode").containsExactly("26734");
        assertThat(regionService.getWards("79", "79-1")).extracting("wardCode").containsExactly("26734");
        assertThat(regionService.getWards("01", "01-1")).extracting("wardCode").containsExactly("00001");

        verify(wardRepository, times(1)).findByCityCityCodeOrderByNameAsc("79-1");
        verify(wardRepository, times(1)).findByCityCityCodeOrderByNameAsc("01-1");
    }

    @Test
    void getActiveReportReasons_success_usesDifferentCacheKeysPerTargetType_TC004() {
        when(reportReasonRepository.findActiveReasonsForTargetType(ReportTargetType.BLOG))
                .thenReturn(List.of(reason("SPAM", ReportTargetType.BLOG)));
        when(reportReasonRepository.findActiveReasonsForTargetType(ReportTargetType.USER))
                .thenReturn(List.of(reason("FAKE_ACCOUNT", ReportTargetType.USER)));

        assertThat(reportReasonService.getActiveReportReasons(ReportTargetType.BLOG))
                .extracting("code").containsExactly("SPAM");
        assertThat(reportReasonService.getActiveReportReasons(ReportTargetType.BLOG))
                .extracting("code").containsExactly("SPAM");
        assertThat(reportReasonService.getActiveReportReasons(ReportTargetType.USER))
                .extracting("code").containsExactly("FAKE_ACCOUNT");

        verify(reportReasonRepository, times(1)).findActiveReasonsForTargetType(ReportTargetType.BLOG);
        verify(reportReasonRepository, times(1)).findActiveReasonsForTargetType(ReportTargetType.USER);
    }

    private void clearCache(String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
    }

    private RegionProvince province(String provinceCode, String name) {
        RegionProvince province = new RegionProvince();
        province.setProvinceCode(provinceCode);
        province.setName(name);
        return province;
    }

    private RegionCity city(String cityCode, String provinceCode, String name) {
        RegionCity city = new RegionCity();
        city.setCityCode(cityCode);
        city.setProvince(province(provinceCode, name));
        city.setName(name);
        return city;
    }

    private RegionWard ward(String wardCode, String provinceCode, String cityCode, String name) {
        RegionWard ward = new RegionWard();
        ward.setWardCode(wardCode);
        ward.setProvince(province(provinceCode, name));
        ward.setCity(city(cityCode, provinceCode, name));
        ward.setName(name);
        return ward;
    }

    private ReportReason reason(String code, ReportTargetType targetType) {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode(code);
        reason.setLabelVi(code);
        reason.setTargetType(targetType);
        reason.setSeverity(1);
        reason.setRequiresDescription(false);
        reason.setActive(true);
        reason.setSortOrder(1);
        return reason;
    }

    @Configuration
    @EnableCaching
    static class TestCacheConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    CacheConfig.REGION_PROVINCES_CACHE,
                    CacheConfig.REGION_CITIES_CACHE,
                    CacheConfig.REGION_WARDS_CACHE,
                    CacheConfig.REPORT_REASONS_CACHE);
        }

        @Bean
        RegionService regionService(
                RegionRepository regionRepository,
                RegionProvinceRepository provinceRepository,
                RegionCityRepository cityRepository,
                RegionWardRepository wardRepository) {
            return new RegionServiceImpl(regionRepository, provinceRepository, cityRepository, wardRepository);
        }

        @Bean
        ReportReasonService reportReasonService(ReportReasonRepository reportReasonRepository) {
            return new ReportReasonServiceImpl(reportReasonRepository);
        }

        @Bean
        RegionRepository regionRepository() {
            return Mockito.mock(RegionRepository.class);
        }

        @Bean
        RegionProvinceRepository provinceRepository() {
            return Mockito.mock(RegionProvinceRepository.class);
        }

        @Bean
        RegionCityRepository cityRepository() {
            return Mockito.mock(RegionCityRepository.class);
        }

        @Bean
        RegionWardRepository wardRepository() {
            return Mockito.mock(RegionWardRepository.class);
        }

        @Bean
        ReportReasonRepository reportReasonRepository() {
            return Mockito.mock(ReportReasonRepository.class);
        }
    }
}
