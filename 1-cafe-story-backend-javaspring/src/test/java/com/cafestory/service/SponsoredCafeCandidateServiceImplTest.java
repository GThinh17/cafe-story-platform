package com.cafestory.service;

import com.cafestory.dto.responseDTO.SponsoredCafeResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdDailyStat;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdStatus;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.repository.AdCampaignRepository;
import com.cafestory.repository.AdDailyStatRepository;
import com.cafestory.repository.AdImpressionRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.SponsoredCafeCandidateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class SponsoredCafeCandidateServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VALID_CAMPAIGN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CAPPED_CAMPAIGN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private AdCampaignRepository adCampaignRepository;

    @Mock
    private AdImpressionRepository adImpressionRepository;

    @Mock
    private AdDailyStatRepository adDailyStatRepository;

    @Mock
    private UserRepository userRepository;

    private SponsoredCafeCandidateServiceImpl sponsoredCafeCandidateService;

    @BeforeEach
    void setUp() {
        sponsoredCafeCandidateService = new SponsoredCafeCandidateServiceImpl(
                adCampaignRepository,
                adImpressionRepository,
                adDailyStatRepository,
                userRepository);
    }

    @Test
    void getCandidates_success_activeCampaignReturned_TC001() {
        Region hoChiMinh = region("Ho Chi Minh");
        CafePage cafePage = cafePage(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "Bean House",
                hoChiMinh,
                PageStatus.ACTIVE,
                true);
        AdCampaign campaign = campaign(VALID_CAMPAIGN_ID, cafePage, AdStatus.ACTIVE, 3);
        User user = user(hoChiMinh);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(campaign));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(USER_ID),
                eq(VALID_CAMPAIGN_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(0L);

        List<SponsoredCafeResponseDTO> result = sponsoredCafeCandidateService.getCandidates(USER_ID, 5, 0);

        assertThat(result).hasSize(1);
        SponsoredCafeResponseDTO response = result.get(0);
        assertThat(response.getCampaignId()).isEqualTo(VALID_CAMPAIGN_ID);
        assertThat(response.getCafePageId()).isEqualTo(cafePage.getId());
        assertThat(response.getCafeName()).isEqualTo("Bean House");
        assertThat(response.getCafeAvatarUrl()).isEqualTo("https://cdn.example.com/bean-house-avatar.jpg");
        assertThat(response.getCafeCoverUrl()).isEqualTo("https://cdn.example.com/bean-house-cover.jpg");
        assertThat(response.getImageUrl()).isEqualTo("https://cdn.example.com/sponsored.jpg");
        assertThat(response.getHeadline()).isEqualTo("Sponsored Bean House");
        assertThat(response.getDescription()).isEqualTo("Try our signature coffee.");
        assertThat(response.getCtaLabel()).isEqualTo("View cafe");
        assertThat(response.getTargetUrl()).isEqualTo("/cafes/" + cafePage.getId());
        assertThat(response.getTrackingToken()).isEqualTo(VALID_CAMPAIGN_ID.toString());
    }

    @Test
    void getCandidates_success_excludesInactiveExpiredExhaustedAndFrequencyCapped_TC002() {
        Region hoChiMinh = region("Ho Chi Minh");
        CafePage cafePage = cafePage(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "Valid Cafe",
                hoChiMinh,
                PageStatus.ACTIVE,
                true);
        AdCampaign valid = campaign(VALID_CAMPAIGN_ID, cafePage, AdStatus.ACTIVE, 3);
        AdCampaign inactive = campaign(UUID.fromString("44444444-4444-4444-4444-444444444444"), cafePage, AdStatus.PAUSED, 9);
        AdCampaign expired = campaign(UUID.fromString("55555555-5555-5555-5555-555555555555"), cafePage, AdStatus.ACTIVE, 9);
        expired.setEndAt(LocalDateTime.now().minusDays(1));
        AdCampaign exhausted = campaign(UUID.fromString("66666666-6666-6666-6666-666666666666"), cafePage, AdStatus.ACTIVE, 9);
        exhausted.setServedImpressions(10000);
        AdCampaign capped = campaign(CAPPED_CAMPAIGN_ID, cafePage, AdStatus.ACTIVE, 9);
        User user = user(hoChiMinh);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(inactive, expired, exhausted, capped, valid));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(USER_ID),
                any(UUID.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenAnswer(invocation -> CAPPED_CAMPAIGN_ID.equals(invocation.getArgument(1)) ? 3L : 0L);

        List<SponsoredCafeResponseDTO> result = sponsoredCafeCandidateService.getCandidates(USER_ID, 5, 0);

        assertThat(result).extracting(SponsoredCafeResponseDTO::getCampaignId)
                .containsExactly(VALID_CAMPAIGN_ID);
    }

    @Test
    void getCandidates_success_excludesSuspendedOrInactiveCafePage_TC003() {
        Region hoChiMinh = region("Ho Chi Minh");
        AdCampaign suspendedPageCampaign = campaign(
                UUID.fromString("77777777-7777-7777-7777-777777777777"),
                cafePage(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), "Suspended Cafe", hoChiMinh, PageStatus.SUSPENDED, true),
                AdStatus.ACTIVE,
                5);
        AdCampaign inactivePageCampaign = campaign(
                UUID.fromString("88888888-8888-8888-8888-888888888888"),
                cafePage(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Inactive Cafe", hoChiMinh, PageStatus.ACTIVE, false),
                AdStatus.ACTIVE,
                5);
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(suspendedPageCampaign, inactivePageCampaign));

        List<SponsoredCafeResponseDTO> result = sponsoredCafeCandidateService.getCandidates(null, 5, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void getCandidates_success_dedupesCafePageAndRotatesByOffset_TC004() {
        Region hoChiMinh = region("Ho Chi Minh");
        CafePage firstCafePage = cafePage(
                UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                "First Cafe",
                hoChiMinh,
                PageStatus.ACTIVE,
                true);
        CafePage secondCafePage = cafePage(
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                "Second Cafe",
                hoChiMinh,
                PageStatus.ACTIVE,
                true);
        AdCampaign lowSameCafe = campaign(UUID.fromString("99999999-9999-9999-9999-999999999999"), firstCafePage, AdStatus.ACTIVE, 1);
        AdCampaign highSameCafe = campaign(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), firstCafePage, AdStatus.ACTIVE, 10);
        AdCampaign secondCafe = campaign(UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffffffff"), secondCafePage, AdStatus.ACTIVE, 5);
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(lowSameCafe, secondCafe, highSameCafe));

        List<SponsoredCafeResponseDTO> result = sponsoredCafeCandidateService.getCandidates(null, 2, 1);

        assertThat(result).extracting(SponsoredCafeResponseDTO::getCampaignId)
                .containsExactly(secondCafe.getAdCampaignId(), highSameCafe.getAdCampaignId());
        assertThat(result).extracting(SponsoredCafeResponseDTO::getCafePageId)
                .doesNotHaveDuplicates();
    }

    @Test
    void recordServedImpressions_success_recordsActualDeliveryAndExpiresAtCap_TC005() {
        CafePage cafePage = cafePage(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "Bean House",
                region("Ho Chi Minh"),
                PageStatus.ACTIVE,
                true);
        AdCampaign campaign = campaign(VALID_CAMPAIGN_ID, cafePage, AdStatus.ACTIVE, 1);
        campaign.setServedImpressions(9999);
        AdDailyStat stat = new AdDailyStat();
        stat.setAdCampaign(campaign);
        stat.setStatDate(LocalDate.now());
        stat.setImpressions(4);
        stat.setClicks(2);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(cafePage.getRegion())));
        when(adCampaignRepository.findByIdWithLock(VALID_CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(VALID_CAMPAIGN_ID, LocalDate.now()))
                .thenReturn(Optional.of(stat));

        sponsoredCafeCandidateService.recordServedImpressions(USER_ID, List.of(VALID_CAMPAIGN_ID));

        assertThat(campaign.getServedImpressions()).isEqualTo(10000);
        assertThat(campaign.getStatus()).isEqualTo(AdStatus.EXPIRED);
        assertThat(stat.getImpressions()).isEqualTo(5);
        ArgumentCaptor<com.cafestory.entity.AdImpression> impressionCaptor =
                ArgumentCaptor.forClass(com.cafestory.entity.AdImpression.class);
        verify(adImpressionRepository).save(impressionCaptor.capture());
        assertThat(impressionCaptor.getValue().getUser().getUserId()).isEqualTo(USER_ID);
        verify(adDailyStatRepository).save(stat);
    }

    @Test
    void recordServedImpressions_success_handlesEmptyMissingAndExpiredCampaigns_TC006() {
        sponsoredCafeCandidateService.recordServedImpressions(USER_ID, List.of());
        verify(adCampaignRepository, never()).findByIdWithLock(any());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adCampaignRepository.findByIdWithLock(VALID_CAMPAIGN_ID)).thenReturn(Optional.empty());
        sponsoredCafeCandidateService.recordServedImpressions(USER_ID, List.of(VALID_CAMPAIGN_ID));
        verify(adImpressionRepository, never()).save(any());

        AdCampaign expired = campaign(VALID_CAMPAIGN_ID, cafePage(
                UUID.randomUUID(), "Expired", region("Da Nang"), PageStatus.ACTIVE, true), AdStatus.ACTIVE, 1);
        expired.setEndAt(LocalDateTime.now().minusMinutes(1));
        when(adCampaignRepository.findByIdWithLock(VALID_CAMPAIGN_ID)).thenReturn(Optional.of(expired));
        sponsoredCafeCandidateService.recordServedImpressions(null, List.of(VALID_CAMPAIGN_ID));
        assertThat(expired.getStatus()).isEqualTo(AdStatus.EXPIRED);
    }

    @Test
    void recordServedImpressions_success_createsDailyStatForAnonymousViewer_TC007() {
        CafePage cafePage = cafePage(UUID.randomUUID(), "Anonymous", region("Hue"), PageStatus.ACTIVE, true);
        AdCampaign campaign = campaign(VALID_CAMPAIGN_ID, cafePage, AdStatus.ACTIVE, 1);
        when(adCampaignRepository.findByIdWithLock(VALID_CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(VALID_CAMPAIGN_ID, LocalDate.now()))
                .thenReturn(Optional.empty());

        sponsoredCafeCandidateService.recordServedImpressions(null, List.of(VALID_CAMPAIGN_ID));

        ArgumentCaptor<AdDailyStat> statCaptor = ArgumentCaptor.forClass(AdDailyStat.class);
        verify(adDailyStatRepository).save(statCaptor.capture());
        assertThat(statCaptor.getValue().getImpressions()).isEqualTo(1);
        assertThat(statCaptor.getValue().getAdCampaign()).isEqualTo(campaign);
    }

    @Test
    void getCandidates_success_handlesDefaultsNullScoringAndComparatorTies_TC008() {
        Region userRegion = region("Hue");
        User user = user(userRegion);
        CafePage regionlessPage = cafePage(UUID.randomUUID(), "Regionless", null, PageStatus.ACTIVE, true);
        AdCampaign regionless = campaign(UUID.randomUUID(), regionlessPage, AdStatus.ACTIVE, 1);
        regionless.setPriority(null);
        regionless.setCreatedAt(null);
        regionless.setStartAt(null);
        regionless.setEndAt(null);

        LocalDateTime sharedCreatedAt = LocalDateTime.now().minusDays(2);
        CafePage blankCityPage = cafePage(UUID.randomUUID(), "Blank City", region("  "), PageStatus.ACTIVE, true);
        AdCampaign firstTie = campaign(UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001"), blankCityPage, AdStatus.ACTIVE, 2);
        firstTie.setCreatedAt(sharedCreatedAt);
        AdCampaign secondTie = campaign(UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002"), blankCityPage, AdStatus.ACTIVE, 2);
        secondTie.setCreatedAt(sharedCreatedAt);

        AdCampaign future = campaign(UUID.randomUUID(), blankCityPage, AdStatus.ACTIVE, 10);
        future.setStartAt(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(regionless, firstTie, secondTie, future));
        when(adImpressionRepository.countByUserUserIdAndAdCampaignAdCampaignIdAndShownAtGreaterThanEqualAndShownAtLessThan(
                eq(USER_ID), any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);

        List<SponsoredCafeResponseDTO> result = sponsoredCafeCandidateService.getCandidates(USER_ID, 100, 0);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SponsoredCafeResponseDTO::getCampaignId)
                .doesNotContain(future.getAdCampaignId());
    }

    @Test
    void getCandidates_success_emptyListUsesDefaultLimitAndRecordNullListIsNoop_TC009() {
        when(adCampaignRepository.findActiveCandidates(eq(AdStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThat(sponsoredCafeCandidateService.getCandidates(null, 0, 0)).isEmpty();
        sponsoredCafeCandidateService.recordServedImpressions(USER_ID, null);

        verify(userRepository, never()).findById(USER_ID);
        verify(adCampaignRepository, never()).findByIdWithLock(any());
    }

    @Test
    void recordServedImpressions_success_handlesNullDailyCounterAndInactiveCampaign_TC010() {
        CafePage cafePage = cafePage(UUID.randomUUID(), "Null Counter", region("Hue"), PageStatus.ACTIVE, true);
        AdCampaign active = campaign(VALID_CAMPAIGN_ID, cafePage, AdStatus.ACTIVE, 1);
        AdDailyStat stat = new AdDailyStat();
        stat.setAdCampaign(active);
        stat.setStatDate(LocalDate.now());
        stat.setImpressions(null);
        when(adCampaignRepository.findByIdWithLock(VALID_CAMPAIGN_ID)).thenReturn(Optional.of(active));
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(VALID_CAMPAIGN_ID, LocalDate.now()))
                .thenReturn(Optional.of(stat));

        sponsoredCafeCandidateService.recordServedImpressions(null, List.of(VALID_CAMPAIGN_ID));

        assertThat(stat.getImpressions()).isEqualTo(1);

        AdCampaign paused = campaign(CAPPED_CAMPAIGN_ID, cafePage, AdStatus.PAUSED, 1);
        when(adCampaignRepository.findByIdWithLock(CAPPED_CAMPAIGN_ID)).thenReturn(Optional.of(paused));
        sponsoredCafeCandidateService.recordServedImpressions(null, List.of(CAPPED_CAMPAIGN_ID));
        assertThat(paused.getStatus()).isEqualTo(AdStatus.PAUSED);
    }

    private User user(Region region) {
        User user = new User();
        user.setUserId(USER_ID);
        user.setRegion(region);
        return user;
    }

    private Region region(String city) {
        Region region = new Region();
        region.setCity(city);
        return region;
    }

    private CafePage cafePage(UUID cafePageId, String cafeName, Region region, PageStatus status, boolean pageActive) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setName(cafeName);
        cafePage.setAvatarUrl("https://cdn.example.com/" + slug(cafeName) + "-avatar.jpg");
        cafePage.setCoverUrl("https://cdn.example.com/" + slug(cafeName) + "-cover.jpg");
        cafePage.setRegion(region);
        cafePage.setStatus(status);
        cafePage.setPageActive(pageActive);
        return cafePage;
    }

    private AdCampaign campaign(UUID campaignId, CafePage cafePage, AdStatus status, int priority) {
        AdCampaign campaign = new AdCampaign();
        campaign.setAdCampaignId(campaignId);
        campaign.setCafePage(cafePage);
        campaign.setTitle("Sponsored " + cafePage.getName());
        campaign.setDescription("Try our signature coffee.");
        campaign.setImageUrl("https://cdn.example.com/sponsored.jpg");
        campaign.setTargetUrl("/cafes/" + cafePage.getId());
        campaign.setStatus(status);
        campaign.setStartAt(LocalDateTime.now().minusDays(1));
        campaign.setEndAt(LocalDateTime.now().plusDays(7));
        campaign.setPriority(priority);
        campaign.setMaxImpressions(10000);
        campaign.setServedImpressions(0);
        campaign.setCreatedAt(LocalDateTime.now().minusDays(2));
        return campaign;
    }

    private String slug(String value) {
        return value.toLowerCase().replace(" ", "-");
    }
}
