package com.cafestory.controller;

import com.cafestory.dto.responseDTO.AdminRegionAnalyticsResponseDTO;
import com.cafestory.dto.responseDTO.AdminRevenueAnalyticsResponseDTO;
import com.cafestory.service.serviceInterface.AdminAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/regions")
    public List<AdminRegionAnalyticsResponseDTO> getRegionAnalytics() {
        return adminAnalyticsService.getRegionAnalytics();
    }

    @GetMapping("/revenue")
    public AdminRevenueAnalyticsResponseDTO getRevenueAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        return adminAnalyticsService.getRevenueAnalytics(days);
    }
}
