package com.cafestory.controller;

import com.cafestory.dto.responseDTO.AdminDashboardSummaryResponseDTO;
import com.cafestory.service.serviceInterface.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/summary")
    public AdminDashboardSummaryResponseDTO getSummary() {
        return adminDashboardService.getSummary();
    }
}
