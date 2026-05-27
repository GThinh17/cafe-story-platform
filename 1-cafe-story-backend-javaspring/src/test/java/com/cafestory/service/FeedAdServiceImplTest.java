package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.FeedItemResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdDailyStat;
import com.cafestory.entity.AdTargetRegion;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdStatus;
import com.cafestory.mapper.AdCampaignMapper;
import com.cafestory.repository.AdCampaignRepository;
import com.cafestory.repository.AdDailyStatRepository;
import com.cafestory.repository.AdImpressionRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.FeedAdServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedAdServiceImplTest {

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID campaignId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private AdCampaignRepository adCampaignRepository;

    @Mock
    private AdImpressionRepository adImpressionRepository;

    @Mock
    private AdDailyStatRepository adDailyStatRepository;

    @Mock
    private UserRepository userRepository;

    private FeedAdServiceImpl feedAdService;

    @BeforeEach
    void setUp() {
        feedAdService = new FeedAdServiceImpl(
                adCampaignRepository,
                adImpressionRepository,
                adDailyStatRepository,
                userRepository,
                new AdCampaignMapper());
    }

    @Test
    void insertAdsIntoFeed_success_recordsImpressionAndUpsertsDailyStats_TC001() {
        User user = user(region("Ho Chi Minh", "District 1", "Ben Nghe", "Ho Chi Minh"));
        AdCampaign campaign = campaign();
        AdDailyStat stat = new AdDailyStat();
        stat.setAdCampaign(campaign);
        stat.setStatDate(LocalDate.now());
        stat.setImpressions(4);
        stat.setClicks(1);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(List.of(campaign));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(userId),
                eq(campaignId),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(0L);
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(eq(campaignId), eq(LocalDate.now())))
                .thenReturn(Optional.of(stat));

        List<FeedItemResponseDTO> result = feedAdService.insertAdsIntoFeed(userId, organicPosts(10));

        assertThat(result).anyMatch(item -> "AD".equals(item.getItemType()) && campaignId.equals(item.getAd().getAdCampaignId()));
        assertThat(campaign.getServedImpressions()).isEqualTo(1);
        assertThat(stat.getImpressions()).isEqualTo(5);
        verify(adImpressionRepository).save(any());
        verify(adDailyStatRepository).save(stat);
    }

    @Test
    void insertAdsIntoFeed_success_expiresCampaignAtMaxImpressions_TC002() {
        User user = user(region("Ho Chi Minh", "District 1", "Ben Nghe", "Ho Chi Minh"));
        AdCampaign campaign = campaign();
        campaign.setServedImpressions(9999);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(List.of(campaign));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(userId),
                eq(campaignId),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(0L);
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(eq(campaignId), eq(LocalDate.now())))
                .thenReturn(Optional.empty());

        feedAdService.insertAdsIntoFeed(userId, organicPosts(10));

        assertThat(campaign.getServedImpressions()).isEqualTo(10000);
        assertThat(campaign.getStatus()).isEqualTo(AdStatus.EXPIRED);
    }

    @Test
    void insertAdsIntoFeed_success_honorsFrequencyCap_TC003() {
        User user = user(region("Ho Chi Minh", "District 1", "Ben Nghe", "Ho Chi Minh"));
        AdCampaign campaign = campaign();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(List.of(campaign));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(userId),
                eq(campaignId),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(3L);

        List<FeedItemResponseDTO> result = feedAdService.insertAdsIntoFeed(userId, organicPosts(10));

        assertThat(result).noneMatch(item -> "AD".equals(item.getItemType()));
        verify(adImpressionRepository, never()).save(any());
    }

    @Test
    void insertAdsIntoFeed_success_doesNotRepeatSameCampaignInConsecutiveAdSlots_TC004() {
        User user = user(region("Ho Chi Minh", "District 1", "Ben Nghe", "Ho Chi Minh"));
        AdCampaign campaign = campaign();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(List.of(campaign));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(userId),
                eq(campaignId),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(0L);
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(eq(campaignId), eq(LocalDate.now())))
                .thenReturn(Optional.empty());

        List<FeedItemResponseDTO> result = feedAdService.insertAdsIntoFeed(userId, organicPosts(25));

        assertThat(result.stream().filter(item -> "AD".equals(item.getItemType())).count()).isEqualTo(1);
    }

    @Test
    void calculateRegionScore_success_returnsExpectedLevels_TC005() {
        Region userRegion = region("Ho Chi Minh", "District 1", "Ben Nghe", "Ho Chi Minh");
        assertThat(feedAdService.calculateRegionScore(campaign(target("Ho Chi Minh", "District 1", null, null)), userRegion))
                .isEqualTo(1.0);
        assertThat(feedAdService.calculateRegionScore(campaign(target("Ho Chi Minh", null, null, null)), userRegion))
                .isEqualTo(0.8);
        assertThat(feedAdService.calculateRegionScore(campaign(target(null, null, "Ho Chi Minh", null)), userRegion))
                .isEqualTo(0.5);
        assertThat(feedAdService.calculateRegionScore(globalCampaign(), userRegion)).isEqualTo(0.3);
        assertThat(feedAdService.calculateRegionScore(campaign(target("Da Nang", null, null, null)), userRegion))
                .isEqualTo(0.1);
    }

    private List<BlogFeedResponse> organicPosts(int count) {
        return IntStream.range(0, count).mapToObj(index -> {
            BlogFeedResponse response = new BlogFeedResponse();
            response.setBlogId(UUID.randomUUID());
            return response;
        }).toList();
    }

    private User user(Region region) {
        User user = new User();
        user.setUserId(userId);
        user.setRegion(region);
        return user;
    }

    private Region region(String city, String area, String ward, String province) {
        Region region = new Region();
        region.setCity(city);
        region.setArea(area);
        region.setWard(ward);
        region.setProvince(province);
        return region;
    }

    private AdCampaign campaign() {
        return campaign(target("Ho Chi Minh", "District 1", "Ho Chi Minh", "Ben Nghe"));
    }

    private AdCampaign globalCampaign() {
        AdCampaign campaign = campaign(null);
        campaign.getTargetRegions().clear();
        return campaign;
    }

    private AdCampaign campaign(AdTargetRegion targetRegion) {
        AdCampaign campaign = new AdCampaign();
        campaign.setAdCampaignId(campaignId);
        campaign.setCafePage(cafePage());
        campaign.setTitle("Feed ad");
        campaign.setStatus(AdStatus.ACTIVE);
        campaign.setStartAt(LocalDateTime.now().minusDays(1));
        campaign.setEndAt(LocalDateTime.now().plusDays(29));
        campaign.setPriority(1);
        campaign.setMaxImpressions(10000);
        campaign.setServedImpressions(0);
        campaign.setMaxDurationDays(30);
        if (targetRegion != null) {
            targetRegion.setAdCampaign(campaign);
            campaign.getTargetRegions().add(targetRegion);
        }
        return campaign;
    }

    private AdTargetRegion target(String city, String area, String province, String ward) {
        AdTargetRegion targetRegion = new AdTargetRegion();
        targetRegion.setCity(city);
        targetRegion.setArea(area);
        targetRegion.setProvince(province);
        targetRegion.setWard(ward);
        return targetRegion;
    }

    private CafePage cafePage() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        return cafePage;
    }
}
