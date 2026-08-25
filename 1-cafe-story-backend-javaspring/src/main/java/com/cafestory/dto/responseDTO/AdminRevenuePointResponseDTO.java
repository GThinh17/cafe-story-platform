package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdminRevenuePointResponseDTO {

    private LocalDate date;
    private BigDecimal totalAmount;
    private BigDecimal reviewerRegistrationAmount;
    private BigDecimal cafePageOpeningAmount;
    private BigDecimal advertiseAmount;
}
