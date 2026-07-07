package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.RagTopReviewerResponseDTO;
import com.cafestory.dto.responseDTO.RagTrendingCafeResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceInterface.RagToolService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RagToolServiceImpl implements RagToolService {

    private static final int MAX_LIMIT = 20;
    private static final int DEFAULT_LIMIT = 5;

    private final CafePageRepository cafePageRepository;
    private final ReviewerRepository reviewerRepository;

    public RagToolServiceImpl(CafePageRepository cafePageRepository, ReviewerRepository reviewerRepository) {
        this.cafePageRepository = cafePageRepository;
        this.reviewerRepository = reviewerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RagTrendingCafeResponseDTO> getTrendingCafes(String province, int limit) {
        String normalizedProvince = province == null || province.isBlank() ? null : province.trim();
        List<CafePage> pages = cafePageRepository.findRagTrendingCafePages(
                normalizedProvince, PageRequest.of(0, normalizeLimit(limit)));
        return pages.stream().map(this::toTrendingCafe).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RagTopReviewerResponseDTO> getTopReviewers(int limit) {
        List<Reviewer> reviewers = reviewerRepository.findRagTopReviewers(
                PageRequest.of(0, normalizeLimit(limit)));
        return reviewers.stream().map(this::toTopReviewer).toList();
    }

    private RagTrendingCafeResponseDTO toTrendingCafe(CafePage page) {
        Region region = page.getRegion();
        return new RagTrendingCafeResponseDTO(
                page.getId().toString(),
                page.getName(),
                page.getAddress(),
                region == null ? null : region.getProvince(),
                region == null ? null : region.getCity(),
                page.getFollowerCount(),
                page.getLikeCount(),
                page.getAvatarUrl());
    }

    private RagTopReviewerResponseDTO toTopReviewer(Reviewer reviewer) {
        return new RagTopReviewerResponseDTO(
                reviewer.getReviewerId().toString(),
                reviewer.getUser().getUserName(),
                reviewer.getUser().getUserFullName(),
                reviewer.getUser().getUserAvatar(),
                reviewer.getUser().getUserFollower());
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
