package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminRevenueAnalyticsResponseDTO {

    private int rangeDays;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal reviewerRegistrationAmount;
    private BigDecimal cafePageOpeningAmount;
    private BigDecimal advertiseAmount;
    private List<AdminRevenuePointResponseDTO> daily;
}
