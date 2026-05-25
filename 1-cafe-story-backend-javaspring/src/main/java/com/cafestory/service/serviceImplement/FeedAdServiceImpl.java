package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.FeedItemResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdDailyStat;
import com.cafestory.entity.AdImpression;
import com.cafestory.entity.AdTargetRegion;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdStatus;
import com.cafestory.mapper.AdCampaignMapper;
import com.cafestory.repository.AdCampaignRepository;
import com.cafestory.repository.AdDailyStatRepository;
import com.cafestory.repository.AdImpressionRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.FeedAdService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FeedAdServiceImpl implements FeedAdService {

    private static final int MIN_ORGANIC_GAP = 7;
    private static final int MAX_ORGANIC_GAP = 10;
    private static final int DAILY_FREQUENCY_CAP = 3;

    private final AdCampaignRepository adCampaignRepository;
    private final AdImpressionRepository adImpressionRepository;
    private final AdDailyStatRepository adDailyStatRepository;
    private final UserRepository userRepository;
    private final AdCampaignMapper adCampaignMapper;

    public FeedAdServiceImpl(
            AdCampaignRepository adCampaignRepository,
            AdImpressionRepository adImpressionRepository,
            AdDailyStatRepository adDailyStatRepository,
            UserRepository userRepository,
            AdCampaignMapper adCampaignMapper) {
        this.adCampaignRepository = adCampaignRepository;
        this.adImpressionRepository = adImpressionRepository;
        this.adDailyStatRepository = adDailyStatRepository;
        this.userRepository = userRepository;
        this.adCampaignMapper = adCampaignMapper;
    }

    @Override
    @Transactional
    public List<FeedItemResponseDTO> insertAdsIntoFeed(UUID userId, List<BlogFeedResponse> organicPosts) {
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        Region userRegion = user == null ? null : user.getRegion();
        LocalDateTime now = LocalDateTime.now();
        List<AdCampaign> candidates = adCampaignRepository.findActiveCandidates(AdStatus.ACTIVE, now)
                .stream()
                .filter(campaign -> !isExpired(campaign, now))
                .filter(campaign -> !exceedsFrequencyCap(userId, campaign, now))
                .toList();

        List<FeedItemResponseDTO> result = new ArrayList<>();
        UUID lastAdCampaignId = null;
        int nextAdSlotAfter = nextOrganicGap();
        int organicSinceAd = 0;
        for (BlogFeedResponse organicPost : organicPosts) {
            result.add(blogItem(organicPost));
            organicSinceAd++;
            if (organicSinceAd >= nextAdSlotAfter) {
                Optional<AdCampaign> selected = selectCampaign(candidates, userRegion, lastAdCampaignId, now);
                if (selected.isPresent()) {
                    AdCampaign campaign = selected.get();
                    recordImpression(campaign, user, now);
                    result.add(adItem(campaign));
                    lastAdCampaignId = campaign.getAdCampaignId();
                }
                organicSinceAd = 0;
                nextAdSlotAfter = nextOrganicGap();
            }
        }
        return result;
    }

    public double calculateRegionScore(AdCampaign campaign, Region userRegion) {
        if (campaign.getTargetRegions().isEmpty()) {
            return 0.3;
        }
        if (userRegion == null) {
            return 0.1;
        }
        return campaign.getTargetRegions().stream()
                .mapToDouble(targetRegion -> calculateRegionScore(targetRegion, userRegion))
                .max()
                .orElse(0.1);
    }

    private Optional<AdCampaign> selectCampaign(
            List<AdCampaign> candidates,
            Region userRegion,
            UUID lastAdCampaignId,
            LocalDateTime now) {
        List<ScoredCampaign> scoredCampaigns = candidates.stream()
                .filter(campaign -> !campaign.getAdCampaignId().equals(lastAdCampaignId))
                .map(campaign -> new ScoredCampaign(campaign, calculateScore(campaign, userRegion, now)))
                .filter(scoredCampaign -> scoredCampaign.score() > 0)
                .sorted(Comparator.comparing(ScoredCampaign::score).reversed())
                .toList();
        if (scoredCampaigns.isEmpty()) {
            return Optional.empty();
        }
        double totalWeight = scoredCampaigns.stream().mapToDouble(ScoredCampaign::score).sum();
        double randomWeight = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulative = 0.0;
        for (ScoredCampaign scoredCampaign : scoredCampaigns) {
            cumulative += scoredCampaign.score();
            if (randomWeight <= cumulative) {
                return Optional.of(scoredCampaign.campaign());
            }
        }
        return Optional.of(scoredCampaigns.getFirst().campaign());
    }

    private double calculateScore(AdCampaign campaign, Region userRegion, LocalDateTime now) {
        double regionScore = calculateRegionScore(campaign, userRegion);
        double ctrScore = calculateCtrScore(campaign);
        double freshnessScore = calculateFreshnessScore(campaign, now);
        double pacingScore = calculatePacingScore(campaign, now);
        return regionScore * 50
                + campaign.getPriority() * 10.0
                + ctrScore * 20
                + freshnessScore * 10
                + pacingScore * 10;
    }

    private double calculateRegionScore(AdTargetRegion targetRegion, Region userRegion) {
        if (sameText(targetRegion.getWard(), userRegion.getWard()) || sameText(targetRegion.getArea(), userRegion.getArea())) {
            return 1.0;
        }
        if (sameText(targetRegion.getCity(), userRegion.getCity())) {
            return 0.8;
        }
        if (sameText(targetRegion.getProvince(), userRegion.getProvince())) {
            return 0.5;
        }
        return 0.1;
    }

    private double calculateCtrScore(AdCampaign campaign) {
        int impressions = Math.max(1, campaign.getServedImpressions());
        int clicks = adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(
                        campaign.getAdCampaignId(),
                        LocalDate.now())
                .map(AdDailyStat::getClicks)
                .orElse(0);
        return Math.min(1.0, clicks / (double) impressions);
    }

    private double calculateFreshnessScore(AdCampaign campaign, LocalDateTime now) {
        if (campaign.getStartAt() == null) {
            return 1.0;
        }
        long ageDays = Math.max(0, Duration.between(campaign.getStartAt(), now).toDays());
        return Math.max(0.1, 1.0 - ageDays / 30.0);
    }

    private double calculatePacingScore(AdCampaign campaign, LocalDateTime now) {
        if (campaign.getStartAt() == null) {
            return 1.0;
        }
        long runningDays = Math.max(1, Duration.between(campaign.getStartAt(), now).toDays() + 1);
        double expectedImpressions = (campaign.getMaxImpressions() / (double) campaign.getMaxDurationDays()) * runningDays;
        if (campaign.getServedImpressions() < expectedImpressions) {
            return 1.0;
        }
        return 0.2;
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

    private void recordImpression(AdCampaign campaign, User user, LocalDateTime now) {
        AdImpression impression = new AdImpression();
        impression.setAdCampaign(campaign);
        impression.setUser(user);
        impression.setShownAt(now);
        adImpressionRepository.save(impression);

        campaign.setServedImpressions(campaign.getServedImpressions() + 1);
        if (campaign.getServedImpressions() >= campaign.getMaxImpressions()) {
            campaign.setStatus(AdStatus.EXPIRED);
        }
        adCampaignRepository.save(campaign);
        incrementDailyImpressions(campaign, now.toLocalDate());
    }

    private void incrementDailyImpressions(AdCampaign campaign, LocalDate statDate) {
        AdDailyStat stat = adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(
                        campaign.getAdCampaignId(),
                        statDate)
                .orElseGet(() -> {
                    AdDailyStat newStat = new AdDailyStat();
                    newStat.setAdCampaign(campaign);
                    newStat.setStatDate(statDate);
                    return newStat;
                });
        stat.setImpressions(stat.getImpressions() + 1);
        adDailyStatRepository.save(stat);
    }

    private boolean isExpired(AdCampaign campaign, LocalDateTime now) {
        if (campaign.getServedImpressions() >= campaign.getMaxImpressions()) {
            campaign.setStatus(AdStatus.EXPIRED);
            adCampaignRepository.save(campaign);
            return true;
        }
        if (campaign.getStartAt() != null && !now.isBefore(campaign.getStartAt().plusDays(campaign.getMaxDurationDays()))) {
            campaign.setStatus(AdStatus.EXPIRED);
            adCampaignRepository.save(campaign);
            return true;
        }
        if (campaign.getEndAt() != null && now.isAfter(campaign.getEndAt())) {
            campaign.setStatus(AdStatus.EXPIRED);
            adCampaignRepository.save(campaign);
            return true;
        }
        return false;
    }

    private FeedItemResponseDTO blogItem(BlogFeedResponse blog) {
        FeedItemResponseDTO item = new FeedItemResponseDTO();
        item.setItemType("BLOG");
        item.setBlog(blog);
        return item;
    }

    private FeedItemResponseDTO adItem(AdCampaign campaign) {
        FeedItemResponseDTO item = new FeedItemResponseDTO();
        item.setItemType("AD");
        item.setAd(adCampaignMapper.toResponse(campaign));
        return item;
    }

    private int nextOrganicGap() {
        return ThreadLocalRandom.current().nextInt(MIN_ORGANIC_GAP, MAX_ORGANIC_GAP + 1);
    }

    private boolean sameText(String left, String right) {
        if (left == null || left.isBlank() || right == null || right.isBlank()) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private record ScoredCampaign(AdCampaign campaign, double score) {
    }
}
