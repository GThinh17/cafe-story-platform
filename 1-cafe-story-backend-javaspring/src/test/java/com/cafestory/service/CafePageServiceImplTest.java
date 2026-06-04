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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
        when(regionRepository.findById(region.getRegionId())).thenReturn(java.util.Optional.of(region));
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

        when(cafePageRepository.findActiveCafePagesForRegionalRanking(regionId, "Ho Chi Minh"))
                .thenReturn(List.of(secondCafe, firstCafe));

        List<CafePageRankingResponseDTO> result = cafePageService.getTopCafePages(
                regionId,
                " Ho Chi Minh ",
                10);

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
        when(regionRepository.findById(region.getRegionId())).thenReturn(java.util.Optional.of(region));
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
