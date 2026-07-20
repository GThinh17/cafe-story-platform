package com.cafestory.service.serviceImplement;

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
import com.cafestory.service.serviceInterface.AdminAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    public static final String UNKNOWN_PROVINCE_CODE = "unknown";
    private static final int MAX_RANGE_DAYS = 365;

    private final UserRepository userRepository;
    private final CafePageRepository cafePageRepository;
    private final ReviewerRepository reviewerRepository;
    private final RegionProvinceRepository regionProvinceRepository;
    private final PaymentRepository paymentRepository;

    public AdminAnalyticsServiceImpl(
            UserRepository userRepository,
            CafePageRepository cafePageRepository,
            ReviewerRepository reviewerRepository,
            RegionProvinceRepository regionProvinceRepository,
            PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.cafePageRepository = cafePageRepository;
        this.reviewerRepository = reviewerRepository;
        this.regionProvinceRepository = regionProvinceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRegionAnalyticsResponseDTO> getRegionAnalytics() {
        Map<String, AdminRegionAnalyticsResponseDTO> byProvince = new LinkedHashMap<>();
        regionProvinceRepository.findAll().stream()
                .sorted(Comparator.comparing(RegionProvince::getProvinceCode))
                .forEach(province -> {
                    AdminRegionAnalyticsResponseDTO row = new AdminRegionAnalyticsResponseDTO();
                    row.setProvinceCode(province.getProvinceCode());
                    row.setProvinceName(province.getName());
                    byProvince.put(province.getProvinceCode(), row);
                });

        long groupedUsers = applyCounts(byProvince, userRepository.countUsersByProvince(),
                AdminRegionAnalyticsResponseDTO::setUserCount);
        long groupedCafePages = applyCounts(byProvince, cafePageRepository.countCafePagesByProvince(),
                AdminRegionAnalyticsResponseDTO::setCafePageCount);
        long groupedReviewers = applyCounts(byProvince, reviewerRepository.countReviewersByProvince(),
                AdminRegionAnalyticsResponseDTO::setReviewerCount);

        long unknownUsers = Math.max(userRepository.count() - groupedUsers, 0);
        long unknownCafePages = Math.max(cafePageRepository.count() - groupedCafePages, 0);
        long unknownReviewers = Math.max(reviewerRepository.count() - groupedReviewers, 0);
        if (unknownUsers > 0 || unknownCafePages > 0 || unknownReviewers > 0) {
            AdminRegionAnalyticsResponseDTO unknown = new AdminRegionAnalyticsResponseDTO();
            unknown.setProvinceCode(UNKNOWN_PROVINCE_CODE);
            unknown.setProvinceName("Không xác định");
            unknown.setUserCount(unknownUsers);
            unknown.setCafePageCount(unknownCafePages);
            unknown.setReviewerCount(unknownReviewers);
            byProvince.put(UNKNOWN_PROVINCE_CODE, unknown);
        }

        return new ArrayList<>(byProvince.values());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRevenueAnalyticsResponseDTO getRevenueAnalytics(int days) {
        if (days < 1 || days > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "days must be between 1 and " + MAX_RANGE_DAYS);
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);
        LocalDateTime start = startDate.atStartOfDay();

        Map<LocalDate, AdminRevenuePointResponseDTO> byDate = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            byDate.put(date, emptyPoint(date));
        }

        for (Object[] row : paymentRepository.findPaidRevenueRows(start)) {
            LocalDateTime occurredAt = (LocalDateTime) row[0];
            ExtraFeeType extraFeeType = (ExtraFeeType) row[1];
            UUID adFeeId = (UUID) row[2];
            BigDecimal amount = row[3] == null ? BigDecimal.ZERO : (BigDecimal) row[3];

            AdminRevenuePointResponseDTO point = byDate.get(occurredAt.toLocalDate());
            if (point == null) {
                continue;
            }
            if (adFeeId != null) {
                point.setAdvertiseAmount(point.getAdvertiseAmount().add(amount));
            } else if (extraFeeType == ExtraFeeType.REVIEWER_REGISTRATION) {
                point.setReviewerRegistrationAmount(point.getReviewerRegistrationAmount().add(amount));
            } else if (extraFeeType == ExtraFeeType.CAFE_PAGE_OPENING) {
                point.setCafePageOpeningAmount(point.getCafePageOpeningAmount().add(amount));
            } else {
                continue;
            }
            point.setTotalAmount(point.getTotalAmount().add(amount));
        }

        AdminRevenueAnalyticsResponseDTO response = new AdminRevenueAnalyticsResponseDTO();
        response.setRangeDays(days);
        response.setCurrency("VND");
        response.setDaily(new ArrayList<>(byDate.values()));
        response.setTotalAmount(sum(byDate, AdminRevenuePointResponseDTO::getTotalAmount));
        response.setReviewerRegistrationAmount(sum(byDate, AdminRevenuePointResponseDTO::getReviewerRegistrationAmount));
        response.setCafePageOpeningAmount(sum(byDate, AdminRevenuePointResponseDTO::getCafePageOpeningAmount));
        response.setAdvertiseAmount(sum(byDate, AdminRevenuePointResponseDTO::getAdvertiseAmount));
        return response;
    }

    private long applyCounts(
            Map<String, AdminRegionAnalyticsResponseDTO> byProvince,
            List<Object[]> rows,
            CountSetter setter) {
        long grouped = 0;
        for (Object[] row : rows) {
            String provinceCode = (String) row[0];
            long count = ((Number) row[1]).longValue();
            AdminRegionAnalyticsResponseDTO target = byProvince.get(provinceCode);
            if (target != null) {
                setter.set(target, count);
            }
            grouped += count;
        }
        return grouped;
    }

    private AdminRevenuePointResponseDTO emptyPoint(LocalDate date) {
        AdminRevenuePointResponseDTO point = new AdminRevenuePointResponseDTO();
        point.setDate(date);
        point.setTotalAmount(BigDecimal.ZERO);
        point.setReviewerRegistrationAmount(BigDecimal.ZERO);
        point.setCafePageOpeningAmount(BigDecimal.ZERO);
        point.setAdvertiseAmount(BigDecimal.ZERO);
        return point;
    }

    private BigDecimal sum(
            Map<LocalDate, AdminRevenuePointResponseDTO> byDate,
            java.util.function.Function<AdminRevenuePointResponseDTO, BigDecimal> getter) {
        return byDate.values().stream()
                .map(getter)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @FunctionalInterface
    private interface CountSetter {
        void set(AdminRegionAnalyticsResponseDTO target, long count);
    }
}
