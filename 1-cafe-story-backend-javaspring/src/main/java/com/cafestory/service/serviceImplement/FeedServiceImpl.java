package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.FeedItemResponseDTO;
import com.cafestory.dto.responseDTO.FeedResponseDTO;
import com.cafestory.dto.responseDTO.SponsoredCafeResponseDTO;
import com.cafestory.entity.enums.FeedItemType;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import com.cafestory.service.serviceInterface.FeedService;
import com.cafestory.service.serviceInterface.SponsoredCafeCandidateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FeedServiceImpl implements FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedServiceImpl.class);
    private static final int DEFAULT_FEED_SIZE = 20;
    private static final int MAX_FEED_SIZE = 50;
    private static final int FEED_CURSOR_VERSION = 1;
    private static final int FIRST_AD_SLOT = 6;
    private static final int SECOND_AD_SLOT = 14;
    private static final int MAX_CONSECUTIVE_ORGANIC_TYPE = 2;
    private static final ObjectMapper CURSOR_OBJECT_MAPPER = JsonMapper.builder().build();

    private final BlogFeedRankingService blogFeedRankingService;
    private final SponsoredCafeCandidateService sponsoredCafeCandidateService;

    public FeedServiceImpl(
            BlogFeedRankingService blogFeedRankingService,
            SponsoredCafeCandidateService sponsoredCafeCandidateService) {
        this.blogFeedRankingService = blogFeedRankingService;
        this.sponsoredCafeCandidateService = sponsoredCafeCandidateService;
    }

    @Override
    public FeedResponseDTO getFeed(UUID userId, String cursor, int size) {
        int safeSize = normalizeFeedSize(size);
        FeedCursor feedCursor = decodeFeedCursor(cursor);
        int maxAds = maxAdsForSize(safeSize);
        int organicLimit = safeSize - maxAds;

        FeedResponseDTO organicPage = getOrganicOrPersonalizedPage(userId, feedCursor.organicCursor(), organicLimit);
        List<FeedItemResponseDTO> organicItems = dedupeOrganicItems(organicPage.getItems());
        List<SponsoredCafeResponseDTO> sponsoredCafes = maxAds == 0
                ? List.of()
                : sponsoredCafeCandidateService.getCandidates(userId, maxAds, feedCursor.adOffset());

        List<FeedItemResponseDTO> mixedItems = assembleFeedItems(organicItems, sponsoredCafes, safeSize);
        sponsoredCafeCandidateService.recordServedImpressions(userId, servedCampaignIds(mixedItems));
        FeedResponseDTO response = new FeedResponseDTO();
        response.setItems(mixedItems);
        response.setHasMore(organicPage.getHasMore());
        response.setNextCursor(Boolean.TRUE.equals(organicPage.getHasMore())
                ? encodeFeedCursor(organicPage.getNextCursor(), feedCursor.adOffset() + countAds(mixedItems), seed(userId))
                : null);
        return response;
    }

    private FeedResponseDTO getOrganicOrPersonalizedPage(UUID userId, String cursor, int organicLimit) {
        if (userId == null) {
            return blogFeedRankingService.getOrganicFeed(cursor, organicLimit);
        }

        try {
            return blogFeedRankingService.getPersonalizedFeedPage(
                    userId,
                    TrendWindowType.HOUR_24,
                    null,
                    cursor,
                    organicLimit);
        } catch (RuntimeException error) {
            log.warn("Personalized mixed feed failed; falling back to organic feed. userId={}", userId, error);
            return blogFeedRankingService.getOrganicFeed(cursor, organicLimit);
        }
    }

    private int normalizeFeedSize(int size) {
        if (size <= 0) {
            return DEFAULT_FEED_SIZE;
        }
        return Math.min(size, MAX_FEED_SIZE);
    }

    private int maxAdsForSize(int size) {
        if (size >= 12) {
            return 2;
        }
        if (size >= 6) {
            return 1;
        }
        return 0;
    }

    private List<FeedItemResponseDTO> dedupeOrganicItems(List<FeedItemResponseDTO> organicItems) {
        if (organicItems == null || organicItems.isEmpty()) {
            return List.of();
        }
        Set<UUID> seenBlogIds = new LinkedHashSet<>();
        List<FeedItemResponseDTO> result = new ArrayList<>();
        for (FeedItemResponseDTO item : organicItems) {
            if (item.getBlog() == null || item.getBlog().getBlogId() == null) {
                result.add(item);
                continue;
            }
            if (seenBlogIds.add(item.getBlog().getBlogId())) {
                result.add(item);
            }
        }
        return result;
    }

    private List<FeedItemResponseDTO> assembleFeedItems(
            List<FeedItemResponseDTO> organicItems,
            List<SponsoredCafeResponseDTO> sponsoredCafes,
            int safeSize) {
        List<FeedItemResponseDTO> mixedItems = diversifyOrganicItems(organicItems);
        List<SponsoredCafeResponseDTO> remainingAds = dedupeSponsoredCafes(sponsoredCafes);
        insertSponsoredCafe(mixedItems, remainingAds, FIRST_AD_SLOT, safeSize);
        insertSponsoredCafe(mixedItems, remainingAds, SECOND_AD_SLOT, safeSize);
        while (!remainingAds.isEmpty() && mixedItems.size() < safeSize) {
            mixedItems.add(toSponsoredItem(remainingAds.removeFirst()));
        }
        List<FeedItemResponseDTO> limitedItems = mixedItems.size() > safeSize
                ? new ArrayList<>(mixedItems.subList(0, safeSize))
                : mixedItems;
        assignPositions(limitedItems);
        return limitedItems;
    }

    private List<FeedItemResponseDTO> diversifyOrganicItems(List<FeedItemResponseDTO> organicItems) {
        if (organicItems.size() <= MAX_CONSECUTIVE_ORGANIC_TYPE) {
            return new ArrayList<>(organicItems);
        }

        List<FeedItemResponseDTO> remainingItems = new ArrayList<>(organicItems);
        List<FeedItemResponseDTO> diversifiedItems = new ArrayList<>(organicItems.size());
        while (!remainingItems.isEmpty()) {
            int selectedIndex = selectNextOrganicIndex(remainingItems, diversifiedItems);
            diversifiedItems.add(remainingItems.remove(selectedIndex));
        }
        return diversifiedItems;
    }

    private int selectNextOrganicIndex(
            List<FeedItemResponseDTO> remainingItems,
            List<FeedItemResponseDTO> diversifiedItems) {
        FeedItemType blockedType = blockedOrganicType(diversifiedItems);
        if (blockedType == null) {
            return 0;
        }

        for (int index = 0; index < remainingItems.size(); index++) {
            if (remainingItems.get(index).getItemType() != blockedType) {
                return index;
            }
        }
        return 0;
    }

    private FeedItemType blockedOrganicType(List<FeedItemResponseDTO> diversifiedItems) {
        if (diversifiedItems.size() < MAX_CONSECUTIVE_ORGANIC_TYPE) {
            return null;
        }

        FeedItemType lastType = diversifiedItems.get(diversifiedItems.size() - 1).getItemType();
        for (int offset = 2; offset <= MAX_CONSECUTIVE_ORGANIC_TYPE; offset++) {
            if (diversifiedItems.get(diversifiedItems.size() - offset).getItemType() != lastType) {
                return null;
            }
        }
        return lastType;
    }

    private List<SponsoredCafeResponseDTO> dedupeSponsoredCafes(List<SponsoredCafeResponseDTO> sponsoredCafes) {
        if (sponsoredCafes == null || sponsoredCafes.isEmpty()) {
            return new ArrayList<>();
        }
        Set<UUID> seenCafePageIds = new LinkedHashSet<>();
        List<SponsoredCafeResponseDTO> result = new ArrayList<>();
        for (SponsoredCafeResponseDTO sponsoredCafe : sponsoredCafes) {
            if (sponsoredCafe.getCafePageId() == null || seenCafePageIds.add(sponsoredCafe.getCafePageId())) {
                result.add(sponsoredCafe);
            }
        }
        return result;
    }

    private void insertSponsoredCafe(
            List<FeedItemResponseDTO> mixedItems,
            List<SponsoredCafeResponseDTO> remainingAds,
            int slot,
            int safeSize) {
        if (remainingAds.isEmpty() || mixedItems.size() >= safeSize) {
            return;
        }
        int insertionIndex = Math.min(slot, mixedItems.size());
        SponsoredCafeResponseDTO selectedAd = removeBestAdForSlot(remainingAds, mixedItems, insertionIndex);
        mixedItems.add(insertionIndex, toSponsoredItem(selectedAd));
    }

    private SponsoredCafeResponseDTO removeBestAdForSlot(
            List<SponsoredCafeResponseDTO> remainingAds,
            List<FeedItemResponseDTO> mixedItems,
            int insertionIndex) {
        UUID previousCafePageId = findAdjacentCafePageId(mixedItems, insertionIndex - 1);
        UUID nextCafePageId = findAdjacentCafePageId(mixedItems, insertionIndex);
        for (int index = 0; index < remainingAds.size(); index++) {
            SponsoredCafeResponseDTO candidate = remainingAds.get(index);
            if (!sameId(candidate.getCafePageId(), previousCafePageId)
                    && !sameId(candidate.getCafePageId(), nextCafePageId)) {
                return remainingAds.remove(index);
            }
        }
        return remainingAds.removeFirst();
    }

    private UUID findAdjacentCafePageId(List<FeedItemResponseDTO> mixedItems, int index) {
        if (index < 0 || index >= mixedItems.size()) {
            return null;
        }
        FeedItemResponseDTO item = mixedItems.get(index);
        if (item.getBlog() == null || item.getBlog().getPageId() == null) {
            return null;
        }
        return item.getBlog().getPageId();
    }

    private boolean sameId(UUID left, UUID right) {
        return left != null && left.equals(right);
    }

    private FeedItemResponseDTO toSponsoredItem(SponsoredCafeResponseDTO sponsoredCafe) {
        FeedItemResponseDTO item = new FeedItemResponseDTO();
        item.setItemType(FeedItemType.SPONSORED_CAFE);
        item.setBlog(null);
        item.setAd(sponsoredCafe);
        item.setTrackingToken(sponsoredCafe.getTrackingToken());
        return item;
    }

    private void assignPositions(List<FeedItemResponseDTO> items) {
        for (int index = 0; index < items.size(); index++) {
            items.get(index).setPosition(index);
        }
    }

    private int countAds(List<FeedItemResponseDTO> items) {
        return (int) items.stream()
                .filter(item -> item.getItemType() == FeedItemType.SPONSORED_CAFE)
                .count();
    }

    private List<UUID> servedCampaignIds(List<FeedItemResponseDTO> items) {
        return items.stream()
                .filter(item -> item.getItemType() == FeedItemType.SPONSORED_CAFE)
                .map(FeedItemResponseDTO::getAd)
                .filter(java.util.Objects::nonNull)
                .map(SponsoredCafeResponseDTO::getCampaignId)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private String seed(UUID userId) {
        return userId == null ? "anonymous" : userId.toString();
    }

    private FeedCursor decodeFeedCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new FeedCursor(null, 0, "anonymous");
        }
        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            FeedCursorPayload payload = CURSOR_OBJECT_MAPPER.readValue(json, FeedCursorPayload.class);
            if (payload.version() != FEED_CURSOR_VERSION || payload.adOffset() == null) {
                throw invalidFeedCursor();
            }
            return new FeedCursor(payload.organicCursor(), Math.max(0, payload.adOffset()), payload.seed());
        } catch (ResponseStatusException error) {
            throw error;
        } catch (IllegalArgumentException | JsonProcessingException error) {
            return new FeedCursor(cursor, 0, "anonymous");
        }
    }

    private String encodeFeedCursor(String organicCursor, int adOffset, String seed) {
        FeedCursorPayload payload = new FeedCursorPayload(organicCursor, adOffset, seed, FEED_CURSOR_VERSION);
        String json = CURSOR_OBJECT_MAPPER.valueToTree(payload).toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private ResponseStatusException invalidFeedCursor() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid feed cursor");
    }

    private record FeedCursor(String organicCursor, int adOffset, String seed) {
    }

    private record FeedCursorPayload(
            String organicCursor,
            Integer adOffset,
            String seed,
            int version) {
    }
}
