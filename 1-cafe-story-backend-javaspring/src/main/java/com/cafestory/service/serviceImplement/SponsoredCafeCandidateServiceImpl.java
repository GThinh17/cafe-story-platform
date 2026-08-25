package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.SponsoredCafeResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdDailyStat;
import com.cafestory.entity.AdImpression;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdStatus;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.repository.AdCampaignRepository;
import com.cafestory.repository.AdDailyStatRepository;
import com.cafestory.repository.AdImpressionRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.SponsoredCafeCandidateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SponsoredCafeCandidateServiceImpl implements SponsoredCafeCandidateService {

    private static final int DEFAULT_REQUESTED_COUNT = 5;
    private static final int MAX_REQUESTED_COUNT = 20;
    private static final int DAILY_FREQUENCY_CAP = 3;
    private static final String DEFAULT_CTA_LABEL = "View cafe";

    private final AdCampaignRepository adCampaignRepository;
    private final AdImpressionRepository adImpressionRepository;
    private final AdDailyStatRepository adDailyStatRepository;
    private final UserRepository userRepository;

    public SponsoredCafeCandidateServiceImpl(
            AdCampaignRepository adCampaignRepository,
            AdImpressionRepository adImpressionRepository,
            AdDailyStatRepository adDailyStatRepository,
            UserRepository userRepository) {
        this.adCampaignRepository = adCampaignRepository;
        this.adImpressionRepository = adImpressionRepository;
        this.adDailyStatRepository = adDailyStatRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsoredCafeResponseDTO> getCandidates(UUID userId, int requestedCount, int adOffset) {
        int limit = normalizeRequestedCount(requestedCount);
        LocalDateTime now = LocalDateTime.now();
        Region userRegion = resolveUserRegion(userId);

        List<AdCampaign> eligibleCampaigns = adCampaignRepository.findActiveCandidates(AdStatus.ACTIVE, now)
                .stream()
                .filter(campaign -> isEligibleCampaign(campaign, now))
                .filter(campaign -> isActiveCafePage(campaign.getCafePage()))
                .toList();
        Map<UUID, Long> dailyImpressionCounts = loadDailyImpressionCounts(userId, eligibleCampaigns, now);

        List<ScoredSponsoredCafe> scoredCandidates = eligibleCampaigns.stream()
                .filter(campaign -> dailyImpressionCounts.getOrDefault(campaign.getAdCampaignId(), 0L)
                        < DAILY_FREQUENCY_CAP)
                .map(campaign -> new ScoredSponsoredCafe(campaign, calculateAdScore(campaign, userRegion, now)))
                .sorted(candidateComparator())
                .toList();

        List<ScoredSponsoredCafe> deduped = dedupeByCafePage(scoredCandidates);
        return rotate(deduped, adOffset)
                .stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void recordServedImpressions(UUID userId, List<UUID> campaignIds) {
        if (campaignIds == null || campaignIds.isEmpty()) {
            return;
        }
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        LocalDateTime shownAt = LocalDateTime.now();
        campaignIds.stream().distinct().forEach(campaignId -> recordServedImpression(campaignId, user, shownAt));
    }

    private void recordServedImpression(UUID campaignId, User user, LocalDateTime shownAt) {
        AdCampaign campaign = adCampaignRepository.findByIdWithLock(campaignId).orElse(null);
        if (!isEligibleCampaign(campaign, shownAt)) {
            expireIfNeeded(campaign, shownAt);
            return;
        }

        AdImpression impression = new AdImpression();
        impression.setAdCampaign(campaign);
        impression.setUser(user);
        impression.setShownAt(shownAt);
        adImpressionRepository.save(impression);

        campaign.setServedImpressions(valueOrZero(campaign.getServedImpressions()) + 1);
        expireIfNeeded(campaign, shownAt);
        adCampaignRepository.save(campaign);
        incrementDailyImpressions(campaign, shownAt.toLocalDate());
    }

    private void expireIfNeeded(AdCampaign campaign, LocalDateTime now) {
        if (campaign != null && campaign.getStatus() == AdStatus.ACTIVE
                && (valueOrZero(campaign.getServedImpressions()) >= valueOrZero(campaign.getMaxImpressions())
                || (campaign.getEndAt() != null && !now.isBefore(campaign.getEndAt())))) {
            campaign.setStatus(AdStatus.EXPIRED);
            adCampaignRepository.save(campaign);
        }
    }

    private void incrementDailyImpressions(AdCampaign campaign, LocalDate statDate) {
        AdDailyStat stat = adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(
                        campaign.getAdCampaignId(),
                        statDate)
                .orElseGet(() -> {
                    AdDailyStat created = new AdDailyStat();
                    created.setAdCampaign(campaign);
                    created.setStatDate(statDate);
                    return created;
                });
        stat.setImpressions(valueOrZero(stat.getImpressions()) + 1);
        adDailyStatRepository.save(stat);
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

    private Map<UUID, Long> loadDailyImpressionCounts(
            UUID userId,
            List<AdCampaign> campaigns,
            LocalDateTime now) {
        if (userId == null || campaigns.isEmpty()) {
            return Map.of();
        }
        List<UUID> campaignIds = campaigns.stream()
                .map(AdCampaign::getAdCampaignId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (campaignIds.isEmpty()) {
            return Map.of();
        }
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        return adImpressionRepository.countByUserAndCampaignIdsBetween(userId, campaignIds, dayStart, dayEnd)
                .stream()
                .collect(Collectors.toMap(
                        AdImpressionRepository.CampaignImpressionCountRow::getCampaignId,
                        AdImpressionRepository.CampaignImpressionCountRow::getImpressionCount,
                        Long::max));
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
        response.setImageUrl(campaign.getImageUrl());
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
