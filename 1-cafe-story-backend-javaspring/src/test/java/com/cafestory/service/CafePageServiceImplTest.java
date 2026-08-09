package com.cafestory.service;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogCursorPageResponseDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageRankingResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.mapper.CafePageMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CafePageRatingRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.PageLikeRepository;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.service.serviceImplement.CafePageServiceImpl;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafePageServiceImplTest {

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private PageLikeRepository pageLikeRepository;

    @Mock
    private CafePageRatingRepository cafePageRatingRepository;

    @Mock
    private PageMemberRepository pageMemberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionService regionService;

    @Mock
    private CafePageMapper cafePageMapper;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private CafePageServiceImpl cafePageService;

    @Test
    void createCafePage_success_TC001() {
        CafePageCreateDTO request = createRequest();
        User owner = user(request.getOwnerUserId());
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        CafePageResponseDTO response = response(cafePage.getId(), owner.getUserId());
        Region region = region();
        request.setRegionId(region.getRegionId());

        when(userValidator.validateUserExists(request.getOwnerUserId())).thenReturn(owner);
        when(regionService.resolveExistingRegion(region.getRegionId(), RegionRequirement.FULL_ADDRESS))
                .thenReturn(region);
        when(cafePageMapper.toCafePage(request)).thenReturn(cafePage);
        when(cafePageRepository.save(cafePage)).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        CafePageResponseDTO result = cafePageService.createCafePage(request);

        assertThat(result).isEqualTo(response);
        assertThat(cafePage.getOwner()).isEqualTo(owner);
        assertThat(cafePage.getRegion()).isEqualTo(region);
        verify(userValidator).validateUserActive(owner);
        verify(cafePageValidator).validateUserCanCreateCafePage(owner.getUserId());
        verify(pageMemberRepository).saveAll(anyList());
    }

    @Test
    void createCafePage_fail_ownerAlreadyHasPage_TC002() {
        CafePageCreateDTO request = createRequest();
        User owner = user(request.getOwnerUserId());

        when(userValidator.validateUserExists(request.getOwnerUserId())).thenReturn(owner);
        org.mockito.Mockito.doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "User already owns a cafe page"))
                .when(cafePageValidator)
                .validateUserCanCreateCafePage(owner.getUserId());

        assertThatThrownBy(() -> cafePageService.createCafePage(request))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .satisfies(error -> assertThat(((org.springframework.web.server.ResponseStatusException) error).getStatusCode())
                        .isEqualTo(org.springframework.http.HttpStatus.CONFLICT));
    }

    @Test
    void getAllCafePages_success_TC003() {
        CafePage cafePage = cafePage(UUID.randomUUID(), user(UUID.randomUUID()));
        CafePageResponseDTO response = response(cafePage.getId(), cafePage.getOwner().getUserId());

        when(cafePageRepository.findAll()).thenReturn(List.of(cafePage));
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        List<CafePageResponseDTO> result = cafePageService.getAllCafePages();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getCafePagesByOwnerId_success_TC004() {
        UUID ownerUserId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID(), user(ownerUserId));
        CafePageResponseDTO response = response(cafePage.getId(), ownerUserId);

        when(cafePageRepository.findByOwnerUserId(ownerUserId)).thenReturn(List.of(cafePage));
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        List<CafePageResponseDTO> result = cafePageService.getCafePagesByOwnerId(ownerUserId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(ownerUserId);
    }

    @Test
    void getTopCafePages_success_sortsByScoreAndAssignsRank_TC005() {
        UUID regionId = UUID.randomUUID();
        CafePage firstCafe = cafePage(UUID.randomUUID(), user(UUID.randomUUID()));
        firstCafe.setName("Popular Cafe");
        firstCafe.setFollowerCount(12);
        firstCafe.setLikeCount(20);
        firstCafe.setPageActive(true);
        firstCafe.setStatus(PageStatus.ACTIVE);
        firstCafe.setRegion(region(regionId, "Ho Chi Minh"));
        firstCafe.setCreatedAt(LocalDateTime.now().minusDays(5));
        CafePage secondCafe = cafePage(UUID.randomUUID(), user(UUID.randomUUID()));
        secondCafe.setName("Quiet Cafe");
        secondCafe.setFollowerCount(2);
        secondCafe.setLikeCount(5);
        secondCafe.setPageActive(true);
        secondCafe.setStatus(PageStatus.ACTIVE);
        secondCafe.setRegion(region(regionId, "Ho Chi Minh"));
        secondCafe.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(cafePageRepository.findActiveCafePagesForRegionalRanking(regionId, "Ho Chi Minh", null, null))
                .thenReturn(List.of(secondCafe, firstCafe));

        List<CafePageRankingResponseDTO> result = cafePageService.getTopCafePages(
                regionId,
                " Ho Chi Minh ",
                null,
                null,
                10,
                null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(firstCafe.getId());
        assertThat(result.get(0).getRankPosition()).isEqualTo(1);
        assertThat(result.get(0).getRegionCity()).isEqualTo("Ho Chi Minh");
        assertThat(result.get(0).getRankingScore()).isGreaterThan(result.get(1).getRankingScore());
        assertThat(result.get(1).getId()).isEqualTo(secondCafe.getId());
        assertThat(result.get(1).getRankPosition()).isEqualTo(2);
    }

    @Test
    void getCafePageById_success_TC006() {
        CafePage cafePage = cafePage(UUID.randomUUID(), user(UUID.randomUUID()));
        CafePageResponseDTO response = response(cafePage.getId(), cafePage.getOwner().getUserId());

        when(cafePageValidator.validateCafePageExists(cafePage.getId())).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        CafePageResponseDTO result = cafePageService.getCafePageById(cafePage.getId());

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getCafePageById_success_enrichesViewerFollowAndLikeState_TC007() {
        UUID viewerUserId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID(), user(UUID.randomUUID()));
        CafePageResponseDTO response = response(cafePage.getId(), cafePage.getOwner().getUserId());

        when(cafePageValidator.validateCafePageExists(cafePage.getId())).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);
        when(pageFollowRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePage.getId()))
                .thenReturn(true);
        when(pageLikeRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePage.getId()))
                .thenReturn(true);

        CafePageResponseDTO result = cafePageService.getCafePageById(cafePage.getId(), viewerUserId);

        assertThat(result.getIsFollowing()).isTrue();
        assertThat(result.getIsLiked()).isTrue();
    }

    @Test
    void getCafePageById_success_marksCoOwnerCanManage_TC012() {
        UUID viewerUserId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID(), user(UUID.randomUUID()));
        CafePageResponseDTO response = response(cafePage.getId(), cafePage.getOwner().getUserId());

        when(cafePageValidator.validateCafePageExists(cafePage.getId())).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);
        when(pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                eq(cafePage.getId()),
                eq(viewerUserId),
                eq(com.cafestory.entity.enums.PageMemberStatus.ACTIVE),
                eq(List.of(com.cafestory.entity.PageMember.ROLE_OWNER, com.cafestory.entity.PageMember.ROLE_CO_OWNER))))
                .thenReturn(true);

        CafePageResponseDTO result = cafePageService.getCafePageById(cafePage.getId(), viewerUserId);

        assertThat(result.getCanManage()).isTrue();
    }

    @Test
    void getBlogsByCafePageId_success_TC008() {
        UUID cafePageId = UUID.randomUUID();
        Blog firstBlog = blog(UUID.randomUUID(), cafePageId, LocalDateTime.of(2026, 5, 28, 10, 0));
        Blog extraBlog = blog(UUID.randomUUID(), cafePageId, LocalDateTime.of(2026, 5, 28, 9, 0));
        BlogResponseDTO firstResponse = new BlogResponseDTO();
        firstResponse.setId(firstBlog.getId());
        firstResponse.setPageId(cafePageId);

        when(blogRepository.findPublishedCafePageBlogsFirstPage(cafePageId, PageRequest.of(0, 2)))
                .thenReturn(List.of(firstBlog, extraBlog));
        when(blogMapper.toBlogResponseDTO(firstBlog)).thenReturn(firstResponse);

        BlogCursorPageResponseDTO result = cafePageService.getBlogsByCafePageId(cafePageId, null, 1);

        assertThat(result.getItems()).containsExactly(firstResponse);
        assertThat(result.getHasMore()).isTrue();
        assertThat(result.getNextCursor()).isNotBlank();
        verify(cafePageValidator).validateCafePageExists(cafePageId);
        verify(blogRepository).findPublishedCafePageBlogsFirstPage(cafePageId, PageRequest.of(0, 2));
    }

    @Test
    void getBlogsByCafePageId_success_nextCursor_TC009() {
        UUID cafePageId = UUID.randomUUID();
        UUID afterId = UUID.randomUUID();
        LocalDateTime afterCreatedAt = LocalDateTime.of(2026, 5, 28, 10, 0);
        String cursor = cursor(afterCreatedAt, afterId);
        Blog blog = blog(UUID.randomUUID(), cafePageId, LocalDateTime.of(2026, 5, 28, 9, 0));
        BlogResponseDTO response = new BlogResponseDTO();
        response.setId(blog.getId());

        when(blogRepository.findPublishedCafePageBlogsAfterCursor(
                eq(cafePageId),
                eq(afterCreatedAt),
                eq(afterId),
                eq(PageRequest.of(0, 21)))).thenReturn(List.of(blog));
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        BlogCursorPageResponseDTO result = cafePageService.getBlogsByCafePageId(cafePageId, cursor, 20);

        assertThat(result.getItems()).containsExactly(response);
        assertThat(result.getHasMore()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        verify(cafePageValidator).validateCafePageExists(cafePageId);
    }

    @Test
    void updateCafePage_success_TC010() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        UUID actorUserId = cafePage.getOwner().getUserId();
        CafePageUpdateDTO request = new CafePageUpdateDTO();
        Region region = region();
        request.setRegionId(region.getRegionId());
        request.setName("Updated Cafe");
        request.setAddress("Updated address");
        request.setStatus(PageStatus.ACTIVE);
        CafePageResponseDTO response = response(cafePageId, cafePage.getOwner().getUserId());

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(regionService.resolveExistingRegion(region.getRegionId(), RegionRequirement.FULL_ADDRESS))
                .thenReturn(region);
        when(cafePageRepository.save(cafePage)).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        CafePageResponseDTO result = cafePageService.updateCafePage(cafePageId, actorUserId, request);

        assertThat(result).isEqualTo(response);
        assertThat(cafePage.getName()).isEqualTo("Updated Cafe");
        assertThat(cafePage.getRegion()).isEqualTo(region);
        assertThat(cafePage.getAddress()).isEqualTo("Updated address");
        assertThat(cafePage.getStatus()).isEqualTo(PageStatus.ACTIVE);
    }

    @Test
    void deleteCafePage_success_TC011() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        UUID actorUserId = cafePage.getOwner().getUserId();

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);

        cafePageService.deleteCafePage(cafePageId, actorUserId);

        verify(cafePageRepository).delete(cafePage);
    }

    @Test
    void createCafePage_success_withoutRegionAndWithCoOwners_TC013() {
        CafePageCreateDTO request = createRequest();
        User owner = user(request.getOwnerUserId());
        User coOwner = user(UUID.randomUUID());
        request.setCoOwnerUserIds(java.util.List.of(coOwner.getUserId(), owner.getUserId()));
        CafePage cafePage = cafePage(UUID.randomUUID(), owner);
        CafePageResponseDTO response = response(cafePage.getId(), owner.getUserId());

        when(userValidator.validateUserExists(request.getOwnerUserId())).thenReturn(owner);
        when(userValidator.validateUserExists(coOwner.getUserId())).thenReturn(coOwner);
        when(cafePageMapper.toCafePage(request)).thenReturn(cafePage);
        when(cafePageRepository.save(cafePage)).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        assertThat(cafePageService.createCafePage(request)).isEqualTo(response);

        assertThat(cafePage.getRegion()).isNull();
        org.mockito.ArgumentCaptor<java.util.List<com.cafestory.entity.PageMember>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        verify(pageMemberRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        verify(regionService, never()).resolveExistingRegion(any(), any());
    }

    @Test
    void updateCafePage_success_updatesEveryOptionalField_TC014() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        UUID actorUserId = cafePage.getOwner().getUserId();
        CafePageUpdateDTO request = new CafePageUpdateDTO();
        request.setDescription("Mo ta moi");
        request.setAvatarUrl("https://cdn.example.com/avatar.png");
        request.setCoverUrl("https://cdn.example.com/cover.png");
        CafePageResponseDTO response = response(cafePageId, actorUserId);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(cafePageRepository.save(cafePage)).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response);

        cafePageService.updateCafePage(cafePageId, actorUserId, request);

        assertThat(cafePage.getDescription()).isEqualTo("Mo ta moi");
        assertThat(cafePage.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
        assertThat(cafePage.getCoverUrl()).isEqualTo("https://cdn.example.com/cover.png");
        // Trường không gửi lên thì giữ nguyên.
        assertThat(cafePage.getName()).isEqualTo("Cafe Story");
        assertThat(cafePage.getStatus()).isEqualTo(PageStatus.DRAFT);
        verify(regionService, never()).resolveExistingRegion(any(), any());
    }

    @Test
    void getBlogsByCafePageId_success_normalizesNonPositiveSize_TC015() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(blogRepository.findPublishedCafePageBlogsFirstPage(
                org.mockito.ArgumentMatchers.eq(cafePageId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(java.util.List.of());

        assertThat(cafePageService.getBlogsByCafePageId(cafePageId, null, 0).getItems()).isEmpty();

        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(blogRepository).findPublishedCafePageBlogsFirstPage(
                org.mockito.ArgumentMatchers.eq(cafePageId), captor.capture());
        assertThat(captor.getValue().getPageSize()).isGreaterThan(1);
    }

    @Test
    void getBlogsByCafePageId_fail_invalidCursor_TC016() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);

        String wrongVersion = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"afterCreatedAt\":\"2026-07-01T00:00:00\",\"afterId\":\"%s\",\"version\":99}"
                        .formatted(UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> cafePageService.getBlogsByCafePageId(cafePageId, "khong-phai-base64!!", 10))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> cafePageService.getBlogsByCafePageId(cafePageId, wrongVersion, 10))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void getBlogsByCafePageId_success_blankCursorIsTreatedAsFirstPage_TC017() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(blogRepository.findPublishedCafePageBlogsFirstPage(
                org.mockito.ArgumentMatchers.eq(cafePageId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(java.util.List.of());

        assertThat(cafePageService.getBlogsByCafePageId(cafePageId, "   ", 10).getHasMore()).isFalse();
    }

    @Test
    void getBlogsByCafePageId_success_marksPageFollowingOnceForViewer_TC018() {
        UUID cafePageId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        Blog blog = blog(UUID.randomUUID(), cafePageId, LocalDateTime.now());
        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(blogRepository.findPublishedCafePageBlogsFirstPage(
                org.mockito.ArgumentMatchers.eq(cafePageId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(blog));
        when(pageFollowRepository.existsByUserUserIdAndCafePageId(viewerUserId, cafePageId)).thenReturn(true);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());

        BlogCursorPageResponseDTO result =
                cafePageService.getBlogsByCafePageId(cafePageId, null, 10, viewerUserId);

        assertThat(result.getItems()).singleElement()
                .satisfies(item -> assertThat(item.getIsPageFollowing()).isTrue());
        assertThat(result.getHasMore()).isFalse();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    void searchCafePages_success_blankQueryReturnsEmpty_TC019() {
        assertThat(cafePageService.searchCafePages(null, null)).isEmpty();
        assertThat(cafePageService.searchCafePages("   ", null)).isEmpty();

        verify(cafePageRepository, never()).searchActiveCafePagesByName(any());
    }

    @Test
    void searchCafePages_success_trimsQueryAndMapsResults_TC020() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        when(cafePageRepository.searchActiveCafePagesByName("story")).thenReturn(List.of(cafePage));
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response(cafePageId, UUID.randomUUID()));
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePageId)).thenReturn(4.0);
        when(cafePageRatingRepository.countByCafePageId(cafePageId)).thenReturn(3L);

        assertThat(cafePageService.searchCafePages("  story  ", null)).hasSize(1);
    }

    @Test
    void getActiveCafePages_success_mapsEveryActivePage_TC021() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, user(UUID.randomUUID()));
        when(cafePageRepository.findAllActiveCafePages()).thenReturn(List.of(cafePage));
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(response(cafePageId, UUID.randomUUID()));
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePageId)).thenReturn(null);
        when(cafePageRatingRepository.countByCafePageId(cafePageId)).thenReturn(0L);

        assertThat(cafePageService.getActiveCafePages(null)).hasSize(1);
    }

    private CafePageCreateDTO createRequest() {
        CafePageCreateDTO request = new CafePageCreateDTO();
        request.setOwnerUserId(UUID.randomUUID());
        request.setName("Cafe Story");
        request.setAddress("123 Nguyen Hue");
        request.setDescription("Specialty coffee");
        return request;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("luan123");
        user.setUserPassword("123456");
        user.setUserEmail("luan@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private CafePage cafePage(UUID cafePageId, User owner) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setOwner(owner);
        cafePage.setName("Cafe Story");
        cafePage.setAddress("123 Nguyen Hue");
        cafePage.setStatus(PageStatus.DRAFT);
        cafePage.setLikeCount(0);
        cafePage.setFollowerCount(0);
        return cafePage;
    }

    private Blog blog(UUID blogId, UUID pageId, LocalDateTime createdAt) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setPageId(pageId);
        blog.setAuthor(user(UUID.randomUUID()));
        blog.setContent("Cafe blog");
        blog.setCreatedAt(createdAt);
        return blog;
    }

    private String cursor(LocalDateTime afterCreatedAt, UUID afterId) {
        String json = """
                {"afterCreatedAt":"%s","afterId":"%s","version":1}
                """.formatted(afterCreatedAt, afterId).trim();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private CafePageResponseDTO response(UUID cafePageId, UUID ownerUserId) {
        CafePageResponseDTO response = new CafePageResponseDTO();
        response.setId(cafePageId);
        response.setOwnerUserId(ownerUserId);
        response.setName("Cafe Story");
        response.setAddress("123 Nguyen Hue");
        response.setStatus(PageStatus.DRAFT);
        response.setLikeCount(0);
        response.setFollowerCount(0);
        return response;
    }

    private Region region() {
        return region(UUID.randomUUID(), "HCM");
    }

    private Region region(UUID regionId, String city) {
        Region region = new Region();
        region.setRegionId(regionId);
        region.setCity(city);
        region.setProvince("HCM");
        region.setArea("D1");
        region.setWard("Ben Nghe");
        region.setStreet("Nguyen Hue");
        return region;
    }
}
