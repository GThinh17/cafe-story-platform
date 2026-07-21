package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AdCampaignStatsResponseDTO {
    private UUID campaignId;
    private Integer servedImpressions;
    private Integer maxImpressions;
    private Integer remainingImpressions;
    private Long totalClicks;
    private BigDecimal ctrPercent;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long remainingDays;
    private List<AdDailyStatResponseDTO> dailyStats = new ArrayList<>();
}
