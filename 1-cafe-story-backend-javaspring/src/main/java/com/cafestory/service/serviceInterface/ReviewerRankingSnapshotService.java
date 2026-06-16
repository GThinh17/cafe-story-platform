package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.reviewer.ReviewerRankingSnapshotResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.enums.RankingPeriodType;

import java.util.List;

public interface ReviewerRankingSnapshotService {

    void generateSnapshot(RankingPeriodType periodType);

    void initSnapshotForNewReviewer(Reviewer reviewer);

    List<ReviewerRankingSnapshotResponseDTO> getRanking(String period, RankingPeriodType periodType, int page, int limit);
}
