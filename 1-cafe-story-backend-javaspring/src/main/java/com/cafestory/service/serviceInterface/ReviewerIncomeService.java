package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.payout.ReviewerIncomeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ReviewerIncomeService {

    void generateDailyIncome(LocalDate date);

    Page<ReviewerIncomeResponse> getIncomeByReviewer(UUID reviewerId, String month, Pageable pageable);

    Page<ReviewerIncomeResponse> getAllIncome(String month, Pageable pageable);
}
