package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogFeedResponse;
import com.cafestory.dto.responseDTO.FeedItemResponseDTO;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.dto.responseDTO.SponsoredCafeResponseDTO;
import com.cafestory.entity.enums.FeedItemType;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceImplement.FeedServiceImpl;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.SponsoredCafeCandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private BlogFeedRankingService blogFeedRankingService;

    @Mock
    private SponsoredCafeCandidateService sponsoredCafeCandidateService;

    private FeedServiceImpl feedService;

    @BeforeEach
    void setUp() {
        feedService = new FeedServiceImpl(blogFeedRankingService, sponsoredCafeCandidateService);
    }

    @Test
    void getFeed_success_insertsTwoSponsoredCafesAtSlots_TC001() {
        FeedResponseDTO organicPage = organicPage(18, "organic-next", true);
        SponsoredCafeResponseDTO firstAd = sponsoredCafe(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        SponsoredCafeResponseDTO secondAd = sponsoredCafe(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 18))
                .thenReturn(organicPage);
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 2, 0)).thenReturn(List.of(firstAd, secondAd));

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 20);

        assertThat(result.getItems()).hasSize(20);
        assertThat(result.getItems().get(6).getItemType()).isEqualTo(FeedItemType.SPONSORED_CAFE);
        assertThat(result.getItems().get(6).getAd().getCampaignId()).isEqualTo(firstAd.getCampaignId());
        assertThat(result.getItems().get(14).getItemType()).isEqualTo(FeedItemType.SPONSORED_CAFE);
        assertThat(result.getItems().get(14).getAd().getCampaignId()).isEqualTo(secondAd.getCampaignId());
        assertThat(result.getItems()).extracting(FeedItemResponseDTO::getPosition)
                .containsExactlyElementsOf(IntStream.range(0, 20).boxed().toList());
        assertThat(result.getHasMore()).isTrue();
        assertThat(result.getNextCursor()).isNotBlank();
        verify(sponsoredCafeCandidateService).recordServedImpressions(
                USER_ID,
                List.of(firstAd.getCampaignId(), secondAd.getCampaignId()));
    }

    @Test
    void getFeed_success_noAdsReturnsOrganicOnly_TC002() {
        FeedResponseDTO organicPage = organicPage(20, null, false);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 18))
                .thenReturn(organicPage);
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 2, 0)).thenReturn(List.of());

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 20);

        assertThat(result.getItems()).hasSize(20);
        assertThat(result.getItems()).extracting(FeedItemResponseDTO::getItemType)
                .containsOnly(FeedItemType.USER_BLOG);
        assertThat(result.getHasMore()).isFalse();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    void getFeed_success_nextCursorPassesOrganicCursorAndAdOffset_TC003() {
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 18))
                .thenReturn(organicPage(18, "personalized-next", true));
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 2, 0))
                .thenReturn(List.of(
                        sponsoredCafe(UUID.fromString("44444444-4444-4444-4444-444444444444"), UUID.randomUUID()),
                        sponsoredCafe(UUID.fromString("55555555-5555-5555-5555-555555555555"), UUID.randomUUID())));
        FeedResponseDTO firstPage = feedService.getFeed(USER_ID, null, 20);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, "personalized-next", 18))
                .thenReturn(organicPage(18, null, false));
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 2, 2)).thenReturn(List.of());

        feedService.getFeed(USER_ID, firstPage.getNextCursor(), 20);

        verify(blogFeedRankingService).getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, "personalized-next", 18);
        verify(sponsoredCafeCandidateService).getCandidates(USER_ID, 2, 2);
    }

    @Test
    void getFeed_success_dedupesSponsoredCafePages_TC004() {
        UUID cafePageId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 18))
                .thenReturn(organicPage(18, null, false));
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 2, 0))
                .thenReturn(List.of(
                        sponsoredCafe(UUID.fromString("66666666-6666-6666-6666-666666666666"), cafePageId),
                        sponsoredCafe(UUID.fromString("77777777-7777-7777-7777-777777777777"), cafePageId)));

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 20);

        assertThat(result.getItems().stream()
                .filter(item -> item.getItemType() == FeedItemType.SPONSORED_CAFE)
                .count()).isEqualTo(1);
    }

    @Test
    void getFeed_success_smallSizeAppendsOneAdSafely_TC005() {
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenReturn(organicPage(5, null, false));
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 1, 0))
                .thenReturn(List.of(sponsoredCafe(
                        UUID.fromString("88888888-8888-8888-8888-888888888888"),
                        UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))));

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 6);

        assertThat(result.getItems()).hasSize(6);
        assertThat(result.getItems().get(5).getItemType()).isEqualTo(FeedItemType.SPONSORED_CAFE);
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(sponsoredCafeCandidateService).getCandidates(eq(USER_ID), countCaptor.capture(), eq(0));
        assertThat(countCaptor.getValue()).isEqualTo(1);
    }

    @Test
    void getFeed_success_sizeBelowAdThresholdDoesNotRequestAds_TC006() {
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenReturn(organicPage(5, null, false));

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 5);

        assertThat(result.getItems()).hasSize(5);
        assertThat(result.getItems()).extracting(FeedItemResponseDTO::getItemType)
                .containsOnly(FeedItemType.USER_BLOG);
        verify(sponsoredCafeCandidateService, never()).getCandidates(eq(USER_ID), anyInt(), anyInt());
    }

    @Test
    void getFeed_success_diversifiesCafePageAndUserBlogs_TC007() {
        FeedResponseDTO organicPage = new FeedResponseDTO();
        organicPage.setItems(List.of(
                organicItem(0, FeedItemType.CAFE_PAGE_BLOG),
                organicItem(1, FeedItemType.CAFE_PAGE_BLOG),
                organicItem(2, FeedItemType.CAFE_PAGE_BLOG),
                organicItem(3, FeedItemType.CAFE_PAGE_BLOG),
                organicItem(4, FeedItemType.USER_BLOG),
                organicItem(5, FeedItemType.USER_BLOG)));
        organicPage.setHasMore(false);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenReturn(organicPage);

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 5);

        assertThat(result.getItems()).extracting(FeedItemResponseDTO::getItemType)
                .containsExactly(
                        FeedItemType.CAFE_PAGE_BLOG,
                        FeedItemType.CAFE_PAGE_BLOG,
                        FeedItemType.USER_BLOG,
                        FeedItemType.CAFE_PAGE_BLOG,
                        FeedItemType.CAFE_PAGE_BLOG);
        verify(sponsoredCafeCandidateService, never()).getCandidates(eq(USER_ID), anyInt(), anyInt());
    }

    @Test
    void getFeed_success_anonymousUsesOrganicFeed_TC008() {
        when(blogFeedRankingService.getOrganicFeed(null, null, 5)).thenReturn(organicPage(5, null, false));

        FeedResponseDTO result = feedService.getFeed(null, null, 5);

        assertThat(result.getItems()).hasSize(5);
        verify(blogFeedRankingService).getOrganicFeed(null, null, 5);
        verify(blogFeedRankingService, never()).getPersonalizedFeedPage(
                eq(USER_ID),
                eq(TrendWindowType.HOUR_24),
                eq(null),
                eq(null),
                anyInt());
        verify(sponsoredCafeCandidateService, never()).getCandidates(eq(USER_ID), anyInt(), anyInt());
    }

    @Test
    void getFeed_success_personalizedFailureFallsBackToOrganic_TC009() {
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenThrow(new IllegalStateException("missing recommendation column"));
        when(blogFeedRankingService.getOrganicFeed(USER_ID, null, 5)).thenReturn(organicPage(5, null, false));

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 5);

        assertThat(result.getItems()).hasSize(5);
        assertThat(result.getHasMore()).isFalse();
        verify(blogFeedRankingService).getOrganicFeed(USER_ID, null, 5);
        verify(sponsoredCafeCandidateService, never()).getCandidates(eq(USER_ID), anyInt(), anyInt());
    }

    @Test
    void getFeed_success_handlesInvalidSizeNullItemsAndDuplicateBlogs_TC010() {
        FeedResponseDTO organicPage = new FeedResponseDTO();
        FeedItemResponseDTO noBlogItem = new FeedItemResponseDTO();
        noBlogItem.setItemType(FeedItemType.USER_BLOG);
        FeedItemResponseDTO noBlogIdItem = new FeedItemResponseDTO();
        noBlogIdItem.setItemType(FeedItemType.USER_BLOG);
        noBlogIdItem.setBlog(new BlogFeedResponse());
        FeedItemResponseDTO firstBlog = organicItem(1);
        FeedItemResponseDTO duplicateBlog = organicItem(99);
        duplicateBlog.getBlog().setBlogId(firstBlog.getBlog().getBlogId());
        organicPage.setItems(List.of(noBlogItem, noBlogIdItem, firstBlog, duplicateBlog));
        organicPage.setHasMore(false);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 18))
                .thenReturn(organicPage);

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 0);

        assertThat(result.getItems()).hasSize(3);
        assertThat(result.getItems()).extracting(FeedItemResponseDTO::getPosition)
                .containsExactly(0, 1, 2);
    }

    @Test
    void getFeed_success_capsSizeAndAppendsExtraSponsoredCafes_TC011() {
        FeedResponseDTO organicPage = organicPage(1, null, false);
        List<SponsoredCafeResponseDTO> ads = List.of(
                sponsoredCafe(UUID.fromString("99999999-9999-9999-9999-999999999991"), UUID.randomUUID()),
                sponsoredCafe(UUID.fromString("99999999-9999-9999-9999-999999999992"), UUID.randomUUID()),
                sponsoredCafe(UUID.fromString("99999999-9999-9999-9999-999999999993"), UUID.randomUUID()));
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 48))
                .thenReturn(organicPage);
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 2, 0)).thenReturn(ads);

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 100);

        assertThat(result.getItems()).hasSize(4);
        assertThat(result.getItems().stream()
                .filter(item -> item.getItemType() == FeedItemType.SPONSORED_CAFE)
                .count()).isEqualTo(3);
    }

    @Test
    void getFeed_success_keepsSameTypeWhenNoAlternativeExists_TC012() {
        FeedResponseDTO organicPage = new FeedResponseDTO();
        organicPage.setItems(List.of(
                organicItem(0, FeedItemType.CAFE_PAGE_BLOG),
                organicItem(1, FeedItemType.CAFE_PAGE_BLOG),
                organicItem(2, FeedItemType.CAFE_PAGE_BLOG)));
        organicPage.setHasMore(false);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenReturn(organicPage);

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 5);

        assertThat(result.getItems()).extracting(FeedItemResponseDTO::getItemType)
                .containsExactly(
                        FeedItemType.CAFE_PAGE_BLOG,
                        FeedItemType.CAFE_PAGE_BLOG,
                        FeedItemType.CAFE_PAGE_BLOG);
    }

    @Test
    void getFeed_success_invalidLegacyCursorFallsBackToOrganicCursor_TC013() {
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, "not-base64", 5))
                .thenReturn(organicPage(5, null, false));

        FeedResponseDTO result = feedService.getFeed(USER_ID, "not-base64", 5);

        assertThat(result.getItems()).hasSize(5);
        verify(blogFeedRankingService).getPersonalizedFeedPage(
                USER_ID,
                TrendWindowType.HOUR_24,
                null,
                "not-base64",
                5);
    }

    @Test
    void getFeed_fail_invalidStructuredCursor_TC014() {
        String invalidStructuredCursor = "eyJvcmdhbmljQ3Vyc29yIjpudWxsLCJhZE9mZnNldCI6bnVsbCwic2VlZCI6ImEiLCJ2ZXJzaW9uIjoxfQ";

        assertThatThrownBy(() -> feedService.getFeed(USER_ID, invalidStructuredCursor, 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid feed cursor");
    }

    @Test
    void getFeed_success_anonymousNextCursorUsesAnonymousSeed_TC015() {
        when(blogFeedRankingService.getOrganicFeed(null, null, 5)).thenReturn(organicPage(5, "organic-next", true));

        FeedResponseDTO result = feedService.getFeed(null, null, 5);

        assertThat(result.getNextCursor()).isNotBlank();
        verify(blogFeedRankingService).getOrganicFeed(null, null, 5);
    }

    @Test
    void getFeed_success_handlesNullOrganicItems_TC016() {
        FeedResponseDTO organicPage = new FeedResponseDTO();
        organicPage.setItems(null);
        organicPage.setHasMore(false);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenReturn(organicPage);

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 5);

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getFeed_success_usesFallbackAdWhenAllAdsTouchSameCafePage_TC017() {
        UUID cafePageId = UUID.fromString("abababab-abab-abab-abab-abababababab");
        FeedResponseDTO organicPage = new FeedResponseDTO();
        organicPage.setItems(List.of(
                organicItem(0, FeedItemType.CAFE_PAGE_BLOG, cafePageId),
                organicItem(1, FeedItemType.CAFE_PAGE_BLOG, cafePageId),
                organicItem(2, FeedItemType.CAFE_PAGE_BLOG, cafePageId),
                organicItem(3, FeedItemType.CAFE_PAGE_BLOG, cafePageId),
                organicItem(4, FeedItemType.CAFE_PAGE_BLOG, cafePageId)));
        organicPage.setHasMore(false);
        SponsoredCafeResponseDTO adjacentAd = sponsoredCafe(
                UUID.fromString("91919191-9191-9191-9191-919191919191"),
                cafePageId);
        when(blogFeedRankingService.getPersonalizedFeedPage(USER_ID, TrendWindowType.HOUR_24, null, null, 5))
                .thenReturn(organicPage);
        when(sponsoredCafeCandidateService.getCandidates(USER_ID, 1, 0)).thenReturn(List.of(adjacentAd));

        FeedResponseDTO result = feedService.getFeed(USER_ID, null, 6);

        assertThat(result.getItems()).hasSize(6);
        assertThat(result.getItems().get(5).getItemType()).isEqualTo(FeedItemType.SPONSORED_CAFE);
        assertThat(result.getItems().get(5).getAd().getCafePageId()).isEqualTo(cafePageId);
    }

    private FeedResponseDTO organicPage(int count, String nextCursor, boolean hasMore) {
        FeedResponseDTO response = new FeedResponseDTO();
        response.setItems(IntStream.range(0, count)
                .mapToObj(index -> organicItem(index))
                .toList());
        response.setNextCursor(nextCursor);
        response.setHasMore(hasMore);
        return response;
    }

    private FeedItemResponseDTO organicItem(int index) {
        return organicItem(index, FeedItemType.USER_BLOG);
    }

    private FeedItemResponseDTO organicItem(int index, FeedItemType itemType) {
        return organicItem(
                index,
                itemType,
                itemType == FeedItemType.CAFE_PAGE_BLOG
                        ? UUID.nameUUIDFromBytes(("page-" + index).getBytes())
                        : null);
    }

    private FeedItemResponseDTO organicItem(int index, FeedItemType itemType, UUID pageId) {
        BlogFeedResponse blog = new BlogFeedResponse();
        blog.setBlogId(UUID.nameUUIDFromBytes(("blog-" + index).getBytes()));
        blog.setPageId(pageId);

        FeedItemResponseDTO item = new FeedItemResponseDTO();
        item.setItemType(itemType);
        item.setBlog(blog);
        item.setPosition(index);
        return item;
    }

    private SponsoredCafeResponseDTO sponsoredCafe(UUID campaignId, UUID cafePageId) {
        SponsoredCafeResponseDTO response = new SponsoredCafeResponseDTO();
        response.setCampaignId(campaignId);
        response.setCafePageId(cafePageId);
        response.setCafeName("Sponsored Cafe");
        response.setHeadline("Visit Sponsored Cafe");
        response.setCtaLabel("View cafe");
        response.setTargetUrl("/cafes/" + cafePageId);
        response.setTrackingToken(campaignId.toString());
        return response;
    }
}
