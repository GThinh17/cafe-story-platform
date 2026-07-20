package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.enums.RankingPeriodType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ReviewerRankingSnapshotService {

    void generateSnapshot(RankingPeriodType periodType);

    void generateSnapshot(RankingPeriodType periodType, LocalDate referenceDate);

    void initSnapshotForNewReviewer(Reviewer reviewer);

    Page<ReviewerRankingSnapshotResponseDTO> getRanking(String period, RankingPeriodType periodType, int page, int size);
}
