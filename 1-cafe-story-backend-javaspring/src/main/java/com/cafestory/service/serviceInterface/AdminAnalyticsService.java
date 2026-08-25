package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.AdminRegionAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.AdminRevenueAnalyticsResponseDTO;

import java.util.List;

public interface AdminAnalyticsService {

    List<AdminRegionAnalyticsResponseDTO> getRegionAnalytics();

    AdminRevenueAnalyticsResponseDTO getRevenueAnalytics(int days);
}
