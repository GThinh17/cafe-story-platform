package com.cafestory.service;

import com.cafestory.dto.responseDTO.AdminRegionAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.AdminRevenueAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.AdminRevenuePointResponseDTO;
import com.cafestory.entity.RegionProvince;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.repository.RegionProvinceRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.AdminAnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private ReviewerRepository reviewerRepository;

    @Mock
    private RegionProvinceRepository regionProvinceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminAnalyticsServiceImpl adminAnalyticsService;

    @Test
    void getRegionAnalytics_zeroFillsProvinces_andAddsUnknownBucket_TC001() {
        when(regionProvinceRepository.findAll()).thenReturn(List.of(
                province("79", "Thành phố Hồ Chí Minh"),
                province("01", "Thành phố Hà Nội")));
        when(userRepository.countUsersByProvince())
                .thenReturn(List.<Object[]>of(new Object[]{"01", 5L}));
        when(cafePageRepository.countCafePagesByProvince())
                .thenReturn(List.<Object[]>of(new Object[]{"79", 2L}));
        when(reviewerRepository.countReviewersByProvince()).thenReturn(List.of());
        when(userRepository.count()).thenReturn(8L);
        when(cafePageRepository.count()).thenReturn(2L);
        when(reviewerRepository.count()).thenReturn(1L);

        List<AdminRegionAnalyticsResponseDTO> result = adminAnalyticsService.getRegionAnalytics();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getProvinceCode()).isEqualTo("01");
        assertThat(result.get(0).getUserCount()).isEqualTo(5L);
        assertThat(result.get(0).getCafePageCount()).isZero();
        assertThat(result.get(1).getProvinceCode()).isEqualTo("79");
        assertThat(result.get(1).getCafePageCount()).isEqualTo(2L);

        AdminRegionAnalyticsResponseDTO unknown = result.get(2);
        assertThat(unknown.getProvinceCode()).isEqualTo(AdminAnalyticsServiceImpl.UNKNOWN_PROVINCE_CODE);
        assertThat(unknown.getUserCount()).isEqualTo(3L);
        assertThat(unknown.getCafePageCount()).isZero();
        assertThat(unknown.getReviewerCount()).isEqualTo(1L);
    }

    @Test
    void getRegionAnalytics_noUnknownRow_whenAllCountsGrouped_TC002() {
        when(regionProvinceRepository.findAll()).thenReturn(List.of(province("01", "Thành phố Hà Nội")));
        when(userRepository.countUsersByProvince())
                .thenReturn(List.<Object[]>of(new Object[]{"01", 4L}));
        when(cafePageRepository.countCafePagesByProvince()).thenReturn(List.of());
        when(reviewerRepository.countReviewersByProvince()).thenReturn(List.of());
        when(userRepository.count()).thenReturn(4L);
        when(cafePageRepository.count()).thenReturn(0L);
        when(reviewerRepository.count()).thenReturn(0L);

        List<AdminRegionAnalyticsResponseDTO> result = adminAnalyticsService.getRegionAnalytics();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProvinceCode()).isEqualTo("01");
    }

    @Test
    void getRevenueAnalytics_categorizesAndZeroFillsDays_TC003() {
        LocalDateTime today = LocalDate.now().atTime(10, 0);
        when(paymentRepository.findPaidRevenueRows(any(LocalDateTime.class))).thenReturn(List.of(
                new Object[]{today, ExtraFeeType.REVIEWER_REGISTRATION, null, new BigDecimal("300000")},
                new Object[]{today, ExtraFeeType.CAFE_PAGE_OPENING, null, new BigDecimal("200000")},
                new Object[]{today, null, UUID.randomUUID(), new BigDecimal("500000")},
                new Object[]{today, null, null, new BigDecimal("999999")}));

        AdminRevenueAnalyticsResponseDTO result = adminAnalyticsService.getRevenueAnalytics(7);

        assertThat(result.getRangeDays()).isEqualTo(7);
        assertThat(result.getCurrency()).isEqualTo("VND");
        assertThat(result.getDaily()).hasSize(7);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1000000");
        assertThat(result.getReviewerRegistrationAmount()).isEqualByComparingTo("300000");
        assertThat(result.getCafePageOpeningAmount()).isEqualByComparingTo("200000");
        assertThat(result.getAdvertiseAmount()).isEqualByComparingTo("500000");

        AdminRevenuePointResponseDTO lastDay = result.getDaily().get(6);
        assertThat(lastDay.getDate()).isEqualTo(LocalDate.now());
        assertThat(lastDay.getTotalAmount()).isEqualByComparingTo("1000000");
        AdminRevenuePointResponseDTO firstDay = result.getDaily().get(0);
        assertThat(firstDay.getTotalAmount()).isEqualByComparingTo("0");
    }

    @Test
    void getRevenueAnalytics_rejectsInvalidRange_TC004() {
        assertThatThrownBy(() -> adminAnalyticsService.getRevenueAnalytics(0))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> adminAnalyticsService.getRevenueAnalytics(366))
                .isInstanceOf(ResponseStatusException.class);
    }

    private RegionProvince province(String code, String name) {
        RegionProvince province = new RegionProvince();
        province.setProvinceCode(code);
        province.setName(name);
        return province;
    }
}
