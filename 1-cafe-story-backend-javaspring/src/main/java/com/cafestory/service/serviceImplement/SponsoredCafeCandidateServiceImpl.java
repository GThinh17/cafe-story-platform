package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.SponsoredCafeResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdStatus;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.repository.AdCampaignRepository;
import com.cafestory.repository.AdImpressionRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.SponsoredCafeCandidateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SponsoredCafeCandidateServiceImpl implements SponsoredCafeCandidateService {

    private static final int DEFAULT_REQUESTED_COUNT = 5;
    private static final int MAX_REQUESTED_COUNT = 20;
    private static final int DAILY_FREQUENCY_CAP = 3;
    private static final String DEFAULT_CTA_LABEL = "View cafe";

    private final AdCampaignRepository adCampaignRepository;
    private final AdImpressionRepository adImpressionRepository;
    private final UserRepository userRepository;

    public SponsoredCafeCandidateServiceImpl(
            AdCampaignRepository adCampaignRepository,
            AdImpressionRepository adImpressionRepository,
            UserRepository userRepository) {
        this.adCampaignRepository = adCampaignRepository;
        this.adImpressionRepository = adImpressionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsoredCafeResponseDTO> getCandidates(UUID userId, int requestedCount, int adOffset) {
        int limit = normalizeRequestedCount(requestedCount);
        LocalDateTime now = LocalDateTime.now();
        Region userRegion = resolveUserRegion(userId);

        List<ScoredSponsoredCafe> scoredCandidates = adCampaignRepository.findActiveCandidates(AdStatus.ACTIVE, now)
                .stream()
                .filter(campaign -> isEligibleCampaign(campaign, now))
                .filter(campaign -> isActiveCafePage(campaign.getCafePage()))
                .filter(campaign -> !exceedsFrequencyCap(userId, campaign, now))
                .map(campaign -> new ScoredSponsoredCafe(campaign, calculateAdScore(campaign, userRegion, now)))
                .filter(candidate -> candidate.score() > 0)
                .sorted(candidateComparator())
                .toList();

        List<ScoredSponsoredCafe> deduped = dedupeByCafePage(scoredCandidates);
        return rotate(deduped, adOffset)
                .stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    private int normalizeRequestedCount(int requestedCount) {
        if (requestedCount <= 0) {
            return DEFAULT_REQUESTED_COUNT;
        }
        return Math.min(requestedCount, MAX_REQUESTED_COUNT);
    }

    private Region resolveUserRegion(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(User::getRegion)
                .orElse(null);
    }

    private boolean isActiveCafePage(CafePage cafePage) {
        return cafePage != null
                && cafePage.getStatus() == PageStatus.ACTIVE
                && Boolean.TRUE.equals(cafePage.getPageActive());
    }

    private boolean isEligibleCampaign(AdCampaign campaign, LocalDateTime now) {
        return campaign != null
                && campaign.getStatus() == AdStatus.ACTIVE
                && (campaign.getStartAt() == null || !campaign.getStartAt().isAfter(now))
                && (campaign.getEndAt() == null || !campaign.getEndAt().isBefore(now))
                && valueOrZero(campaign.getServedImpressions()) < valueOrZero(campaign.getMaxImpressions());
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean exceedsFrequencyCap(UUID userId, AdCampaign campaign, LocalDateTime now) {
        if (userId == null) {
            return false;
        }
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        return adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                userId,
                campaign.getAdCampaignId(),
                dayStart,
                dayEnd) >= DAILY_FREQUENCY_CAP;
    }

    private double calculateAdScore(AdCampaign campaign, Region userRegion, LocalDateTime now) {
        double regionScore = sameCity(campaign.getCafePage(), userRegion) ? 30.0 : 0.0;
        double priorityScore = campaign.getPriority() == null ? 0.0 : campaign.getPriority();
        double freshnessScore = calculateFreshnessScore(campaign, now);
        return regionScore + priorityScore + freshnessScore;
    }

    private boolean sameCity(CafePage cafePage, Region userRegion) {
        if (cafePage == null || cafePage.getRegion() == null || userRegion == null) {
            return false;
        }
        return sameText(cafePage.getRegion().getCity(), userRegion.getCity());
    }

    private boolean sameText(String left, String right) {
        if (left == null || left.isBlank() || right == null || right.isBlank()) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private double calculateFreshnessScore(AdCampaign campaign, LocalDateTime now) {
        if (campaign.getCreatedAt() == null) {
            return 0.0;
        }
        long ageDays = Math.max(0, Duration.between(campaign.getCreatedAt(), now).toDays());
        return Math.exp(-ageDays / 14.0) * 5.0;
    }

    private Comparator<ScoredSponsoredCafe> candidateComparator() {
        return Comparator.comparingDouble(ScoredSponsoredCafe::score).reversed()
                .thenComparing(
                        candidate -> candidate.campaign().getCreatedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(
                        candidate -> candidate.campaign().getAdCampaignId(),
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private List<ScoredSponsoredCafe> dedupeByCafePage(List<ScoredSponsoredCafe> candidates) {
        Map<UUID, ScoredSponsoredCafe> byCafePage = new LinkedHashMap<>();
        for (ScoredSponsoredCafe candidate : candidates) {
            UUID cafePageId = candidate.campaign().getCafePage().getId();
            byCafePage.putIfAbsent(cafePageId, candidate);
        }
        return List.copyOf(byCafePage.values());
    }

    private List<ScoredSponsoredCafe> rotate(List<ScoredSponsoredCafe> candidates, int adOffset) {
        if (candidates.isEmpty()) {
            return candidates;
        }
        int offset = Math.floorMod(adOffset, candidates.size());
        if (offset == 0) {
            return candidates;
        }
        return java.util.stream.Stream.concat(
                        candidates.subList(offset, candidates.size()).stream(),
                        candidates.subList(0, offset).stream())
                .toList();
    }

    private SponsoredCafeResponseDTO toResponse(ScoredSponsoredCafe scoredCandidate) {
        AdCampaign campaign = scoredCandidate.campaign();
        CafePage cafePage = campaign.getCafePage();
        SponsoredCafeResponseDTO response = new SponsoredCafeResponseDTO();
        response.setCampaignId(campaign.getAdCampaignId());
        response.setCafePageId(cafePage.getId());
        response.setCafeName(cafePage.getName());
        response.setCafeAvatarUrl(cafePage.getAvatarUrl());
        response.setCafeCoverUrl(cafePage.getCoverUrl());
        response.setHeadline(campaign.getTitle());
        response.setDescription(campaign.getDescription());
        response.setCtaLabel(DEFAULT_CTA_LABEL);
        response.setTargetUrl(campaign.getTargetUrl());
        response.setTrackingToken(campaign.getAdCampaignId().toString());
        return response;
    }

    private record ScoredSponsoredCafe(AdCampaign campaign, double score) {
    }
}
