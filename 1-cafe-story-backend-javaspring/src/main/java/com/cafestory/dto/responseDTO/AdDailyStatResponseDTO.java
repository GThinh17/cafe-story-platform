package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdDailyStatResponseDTO {
    private LocalDate statDate;
    private Integer impressions;
    private Integer clicks;
}
