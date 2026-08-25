package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogCursorPageResponseDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageRankingResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.PageMemberId;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.mapper.CafePageMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CafePageRatingRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.PageLikeRepository;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.service.serviceInterface.CafePageService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CafePageServiceImpl implements CafePageService {
    private static final int DEFAULT_BLOG_PAGE_SIZE = 20;
    private static final int MAX_BLOG_PAGE_SIZE = 50;
    private static final int CURSOR_VERSION = 1;
    private static final ObjectMapper CURSOR_OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private final CafePageRepository cafePageRepository;
    private final BlogRepository blogRepository;
    private final PageFollowRepository pageFollowRepository;
    private final PageLikeRepository pageLikeRepository;
    private final CafePageRatingRepository cafePageRatingRepository;
    private final PageMemberRepository pageMemberRepository;
    private final RegionService regionService;
    private final CafePageMapper cafePageMapper;
    private final BlogMapper blogMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public CafePageServiceImpl(
            CafePageRepository cafePageRepository,
            BlogRepository blogRepository,
            PageFollowRepository pageFollowRepository,
            PageLikeRepository pageLikeRepository,
            CafePageRatingRepository cafePageRatingRepository,
            PageMemberRepository pageMemberRepository,
            RegionService regionService,
            CafePageMapper cafePageMapper,
            BlogMapper blogMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.cafePageRepository = cafePageRepository;
        this.blogRepository = blogRepository;
        this.pageFollowRepository = pageFollowRepository;
        this.pageLikeRepository = pageLikeRepository;
        this.cafePageRatingRepository = cafePageRatingRepository;
        this.pageMemberRepository = pageMemberRepository;
        this.regionService = regionService;
        this.cafePageMapper = cafePageMapper;
        this.blogMapper = blogMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true)
    })
    public CafePageResponseDTO createCafePage(CafePageCreateDTO cafePageCreateDTO) {
        User owner = userValidator.validateUserExists(cafePageCreateDTO.getOwnerUserId());
        userValidator.validateUserActive(owner);
        cafePageValidator.validateUserCanCreateCafePage(owner.getUserId());

        CafePage cafePage = cafePageMapper.toCafePage(cafePageCreateDTO);
        cafePage.setOwner(owner);
        if (cafePageCreateDTO.getRegionId() != null) {
            cafePage.setRegion(regionService.resolveExistingRegion(
                    cafePageCreateDTO.getRegionId(),
                    RegionRequirement.FULL_ADDRESS));
        }

        CafePage savedCafePage = cafePageRepository.save(cafePage);
        pageMemberRepository.saveAll(createPageMembers(savedCafePage, owner, cafePageCreateDTO.getCoOwnerUserIds()));
        return toCafePageResponseDTO(savedCafePage, owner.getUserId());
    }

    private List<PageMember> createPageMembers(CafePage cafePage, User owner, List<UUID> coOwnerUserIds) {
        Set<UUID> memberUserIds = new LinkedHashSet<>();
        memberUserIds.add(owner.getUserId());
        if (coOwnerUserIds != null) {
            memberUserIds.addAll(coOwnerUserIds);
        }

        return memberUserIds.stream()
                .map(userId -> createPageMember(cafePage, owner, userId))
                .toList();
    }

    private PageMember createPageMember(CafePage cafePage, User owner, UUID userId) {
        User memberUser = owner;
        if (!owner.getUserId().equals(userId)) {
            memberUser = userValidator.validateUserExists(userId);
            userValidator.validateUserActive(memberUser);
        }

        PageMember pageMember = new PageMember();
        pageMember.setId(new PageMemberId(cafePage.getId(), memberUser.getUserId()));
        pageMember.setCafePage(cafePage);
        pageMember.setUser(memberUser);
        pageMember.setRoleName(owner.getUserId().equals(memberUser.getUserId())
                ? PageMember.ROLE_OWNER
                : PageMember.ROLE_CO_OWNER);
        pageMember.setStatus(PageMemberStatus.ACTIVE);
        return pageMember;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getAllCafePages() {
        return getAllCafePages(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, key = "'all:' + (#p0 == null ? 'anon' : #p0)")
    public List<CafePageResponseDTO> getAllCafePages(UUID viewerUserId) {
        return cafePageRepository.findAll()
                .stream()
                .map(cafePage -> toCafePageResponseDTO(cafePage, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getCafePagesByOwnerId(UUID ownerUserId) {
        return getCafePagesByOwnerId(ownerUserId, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, key = "'owner:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1)")
    public List<CafePageResponseDTO> getCafePagesByOwnerId(UUID ownerUserId, UUID viewerUserId) {
        userValidator.validateUserExists(ownerUserId);
        return cafePageRepository.findByOwnerUserId(ownerUserId)
                .stream()
                .map(cafePage -> toCafePageResponseDTO(cafePage, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // The viewer belongs in the key: the response carries isFollowing, which is
    // per-viewer, so leaving p5 out served the first caller's follow state to
    // everyone else looking at the same region.
    @Cacheable(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, key = "'top:' + (#p0 == null ? 'none' : #p0) + ':' + (#p1 == null ? 'none' : #p1) + ':' + (#p2 == null ? 'none' : #p2) + ':' + (#p3 == null ? 'none' : #p3) + ':' + #p4 + ':' + (#p5 == null ? 'anon' : #p5)")
    public List<CafePageRankingResponseDTO> getTopCafePages(UUID regionId, String city, String area, String province, int size, UUID viewerUserId) {
        String normalizedCity = normalizeString(city);
        String normalizedArea = normalizeString(area);
        String normalizedProvince = normalizeString(province);
        int safeSize = Math.min(Math.max(1, size), 50);
        LocalDateTime now = LocalDateTime.now();
        List<CafePageRankingCandidate> candidates = cafePageRepository.findActiveCafePagesForRegionalRanking(
                regionId,
                normalizedCity,
                normalizedArea,
                normalizedProvince)
                .stream()
                .map(cafePage -> new CafePageRankingCandidate(cafePage, calculateRankingScore(cafePage, now)))
                .sorted(Comparator.comparing(CafePageRankingCandidate::score).reversed()
                        .thenComparing(candidate -> safe(candidate.cafePage().getFollowerCount()),
                                Comparator.reverseOrder())
                        .thenComparing(candidate -> safe(candidate.cafePage().getLikeCount()),
                                Comparator.reverseOrder())
                        .thenComparing(candidate -> candidate.cafePage().getCreatedAt(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeSize)
                .toList();

        List<CafePageRankingResponseDTO> responses = new ArrayList<>();
        for (int index = 0; index < candidates.size(); index++) {
            CafePageRankingCandidate candidate = candidates.get(index);
            responses.add(toRankingResponse(candidate.cafePage(), candidate.score(), index + 1, viewerUserId));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public CafePageResponseDTO getCafePageById(UUID cafePageId) {
        return getCafePageById(cafePageId, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, key = "'detail:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1)")
    public CafePageResponseDTO getCafePageById(UUID cafePageId, UUID viewerUserId) {
        return toCafePageResponseDTO(cafePageValidator.validateCafePageExists(cafePageId), viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, key = "#p0 + ':' + (#p1 == null ? 'first' : #p1) + ':' + #p2")
    public BlogCursorPageResponseDTO getBlogsByCafePageId(UUID cafePageId, String cursor, int size) {
        return getBlogsByCafePageId(cafePageId, cursor, size, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogCursorPageResponseDTO getBlogsByCafePageId(UUID cafePageId, String cursor, int size, UUID viewerUserId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        int safeSize = normalizeBlogPageSize(size);
        CafePageBlogCursor pageCursor = decodeCursor(cursor);
        List<Blog> blogs = findCafePageBlogs(cafePageId, pageCursor, safeSize + 1);
        boolean hasMore = blogs.size() > safeSize;
        List<Blog> pageItems = hasMore ? blogs.subList(0, safeSize) : blogs;
        // All blogs in this list belong to cafePageId — check page follow once
        boolean isPageFollowing = viewerUserId != null
                && pageFollowRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePageId);
        List<BlogResponseDTO> items = pageItems
                .stream()
                .map(blog -> {
                    BlogResponseDTO dto = blogMapper.toBlogResponseDTO(blog);
                    dto.setIsPageFollowing(isPageFollowing);
                    return dto;
                })
                .toList();

        BlogCursorPageResponseDTO response = new BlogCursorPageResponseDTO();
        response.setItems(items);
        response.setHasMore(hasMore);
        response.setNextCursor(hasMore && !pageItems.isEmpty()
                ? encodeCursor(pageItems.getLast())
                : null);
        return response;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true)
    })
    public CafePageResponseDTO updateCafePage(UUID cafePageId, UUID actorUserId, CafePageUpdateDTO cafePageUpdateDTO) {
        cafePageValidator.validateUserCanManagePage(cafePageId, actorUserId);
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);

        if (cafePageUpdateDTO.getRegionId() != null) {
            cafePage.setRegion(regionService.resolveExistingRegion(
                    cafePageUpdateDTO.getRegionId(),
                    RegionRequirement.FULL_ADDRESS));
        }
        if (cafePageUpdateDTO.getName() != null) {
            cafePage.setName(cafePageUpdateDTO.getName());
        }
        if (cafePageUpdateDTO.getAddress() != null) {
            cafePage.setAddress(cafePageUpdateDTO.getAddress());
        }
        if (cafePageUpdateDTO.getDescription() != null) {
            cafePage.setDescription(cafePageUpdateDTO.getDescription());
        }
        if (cafePageUpdateDTO.getAvatarUrl() != null) {
            cafePage.setAvatarUrl(cafePageUpdateDTO.getAvatarUrl());
        }
        if (cafePageUpdateDTO.getCoverUrl() != null) {
            cafePage.setCoverUrl(cafePageUpdateDTO.getCoverUrl());
        }
        if (cafePageUpdateDTO.getStatus() != null) {
            cafePage.setStatus(cafePageUpdateDTO.getStatus());
        }

        CafePage updatedCafePage = cafePageRepository.save(cafePage);
        return toCafePageResponseDTO(updatedCafePage, actorUserId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true)
    })
    public void deleteCafePage(UUID cafePageId, UUID actorUserId) {
        cafePageValidator.validateUserCanManagePage(cafePageId, actorUserId);
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        cafePageRepository.delete(cafePage);
    }

    private int normalizeBlogPageSize(int size) {
        if (size <= 0) {
            return DEFAULT_BLOG_PAGE_SIZE;
        }
        return Math.min(size, MAX_BLOG_PAGE_SIZE);
    }

    private List<Blog> findCafePageBlogs(UUID cafePageId, CafePageBlogCursor cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (cursor == null) {
            return blogRepository.findPublishedCafePageBlogsFirstPage(cafePageId, pageRequest);
        }

        return blogRepository.findPublishedCafePageBlogsAfterCursor(
                cafePageId,
                cursor.afterCreatedAt(),
                cursor.afterId(),
                pageRequest);
    }

    private CafePageBlogCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            CafePageBlogCursorPayload payload = CURSOR_OBJECT_MAPPER.readValue(json, CafePageBlogCursorPayload.class);
            if (payload.version() != CURSOR_VERSION || payload.afterCreatedAt() == null || payload.afterId() == null) {
                throw invalidCursor();
            }
            return new CafePageBlogCursor(LocalDateTime.parse(payload.afterCreatedAt()), payload.afterId());
        } catch (IllegalArgumentException | JsonProcessingException | DateTimeParseException error) {
            throw invalidCursor();
        }
    }

    private String encodeCursor(Blog blog) {
        try {
            CafePageBlogCursorPayload payload = new CafePageBlogCursorPayload(
                    blog.getCreatedAt().toString(),
                    blog.getId(),
                    CURSOR_VERSION);
            String json = CURSOR_OBJECT_MAPPER.writeValueAsString(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create blog cursor");
        }
    }

    private ResponseStatusException invalidCursor() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cafe page blog cursor");
    }

    private record CafePageBlogCursor(LocalDateTime afterCreatedAt, UUID afterId) {
    }

    private record CafePageBlogCursorPayload(String afterCreatedAt, UUID afterId, int version) {
    }

    private double calculateRankingScore(CafePage cafePage, LocalDateTime now) {
        return safe(cafePage.getFollowerCount()) * 3.0
                + safe(cafePage.getLikeCount()) * 2.0
                + freshnessScore(cafePage.getCreatedAt(), now);
    }

    private double freshnessScore(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null) {
            return 0.0;
        }
        double ageDays = Math.max(0, Duration.between(createdAt, now).toHours() / 24.0);
        return Math.exp(-ageDays / 90.0) * 10.0;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> searchCafePages(String query, UUID viewerUserId) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return cafePageRepository.searchActiveCafePagesByName(query.trim())
                .stream()
                .map(cafePage -> toCafePageResponseDTO(cafePage, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getActiveCafePages(UUID viewerUserId) {
        return cafePageRepository.findAllActiveCafePages()
                .stream()
                .map(cafePage -> toCafePageResponseDTO(cafePage, viewerUserId))
                .toList();
    }

    private String normalizeString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CafePageRankingResponseDTO toRankingResponse(CafePage cafePage, double rankingScore, int rankPosition, UUID viewerUserId) {
        Region region = cafePage.getRegion();
        CafePageRankingResponseDTO response = new CafePageRankingResponseDTO();
        response.setId(cafePage.getId());
        response.setOwnerUserId(cafePage.getOwner() == null ? null : cafePage.getOwner().getUserId());
        response.setRegionId(region == null ? null : region.getRegionId());
        response.setRegionCity(region == null ? null : region.getCity());
        response.setRegionProvince(region == null ? null : region.getProvince());
        response.setRegionArea(region == null ? null : region.getArea());
        response.setName(cafePage.getName());
        response.setAddress(cafePage.getAddress());
        response.setDescription(cafePage.getDescription());
        response.setAvatarUrl(cafePage.getAvatarUrl());
        response.setCoverUrl(cafePage.getCoverUrl());
        response.setStatus(cafePage.getStatus());
        response.setLikeCount(cafePage.getLikeCount());
        response.setFollowerCount(cafePage.getFollowerCount());
        response.setPageActive(cafePage.getPageActive());
        response.setRankingScore(rankingScore);
        response.setRankPosition(rankPosition);
        response.setIsFollowing(viewerUserId != null && cafePage.getId() != null
                && pageFollowRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePage.getId()));
        response.setCreatedAt(cafePage.getCreatedAt());
        return response;
    }

    private record CafePageRankingCandidate(CafePage cafePage, double score) {
    }

    private CafePageResponseDTO toCafePageResponseDTO(CafePage cafePage, UUID viewerUserId) {
        CafePageResponseDTO response = cafePageMapper.toCafePageResponseDTO(cafePage);
        UUID cafePageId = cafePage.getId();
        response.setCanManage(canManageCafePage(cafePage, viewerUserId));
        response.setIsFollowing(viewerUserId != null
                && cafePageId != null
                && pageFollowRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePageId));
        response.setIsLiked(viewerUserId != null
                && cafePageId != null
                && pageLikeRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePageId));
        response.setRatingScore(resolveRatingScore(cafePageId));
        response.setRatingCount(cafePageRatingRepository.countByCafePageId(cafePageId));
        if (viewerUserId == null) {
            response.setIsRating(false);
            response.setMyRating(null);
            return response;
        }

        cafePageRatingRepository.findByUserUserIdAndCafePageId(viewerUserId, cafePageId)
                .ifPresentOrElse(
                        rating -> {
                            response.setIsRating(true);
                            response.setMyRating(rating.getRating());
                        },
                        () -> {
                            response.setIsRating(false);
                            response.setMyRating(null);
                        });
        return response;
    }

    private double resolveRatingScore(UUID cafePageId) {
        Double ratingScore = cafePageRatingRepository.findAverageRatingByCafePageId(cafePageId);
        return ratingScore == null ? 0.0 : ratingScore;
    }

    private boolean canManageCafePage(CafePage cafePage, UUID viewerUserId) {
        if (viewerUserId == null || cafePage == null || cafePage.getId() == null) {
            return false;
        }
        if (cafePage.getOwner() != null && viewerUserId.equals(cafePage.getOwner().getUserId())) {
            return true;
        }
        return pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                cafePage.getId(),
                viewerUserId,
                PageMemberStatus.ACTIVE,
                List.of(PageMember.ROLE_OWNER, PageMember.ROLE_CO_OWNER));
    }
}
