package com.cafestory.service;

import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerBadgeHistory;
import com.cafestory.entity.ReviewerBadgeThreshold;
import com.cafestory.entity.ReviewerFormula;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ReviewerBadge;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceImplement.RagSnapshotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagSnapshotServiceImplTest {

    @Mock
    private BlogRepository blogRepository;
    @Mock
    private CafePageRepository cafePageRepository;
    @Mock
    private ReviewerRepository reviewerRepository;
    @Mock
    private AiModerationResultRepository aiModerationResultRepository;
    @Mock
    private com.cafestory.repository.ReviewerBadgeHistoryRepository reviewerBadgeHistoryRepository;
    @Mock
    private com.cafestory.repository.ReviewerFormulaRepository reviewerFormulaRepository;
    @Mock
    private com.cafestory.repository.ReviewerBadgeThresholdRepository reviewerBadgeThresholdRepository;

    private RagSnapshotServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RagSnapshotServiceImpl(
                blogRepository, cafePageRepository, reviewerRepository, aiModerationResultRepository,
                reviewerBadgeHistoryRepository, reviewerFormulaRepository, reviewerBadgeThresholdRepository);
    }

    @Test
    void unsupportedSourceTypeThrowsBadRequest() {
        assertThatThrownBy(() -> service.getSnapshot("payments", null, null, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported sourceType");
    }

    @Test
    void blogSnapshotMapsPublicFieldsOnly() {
        Blog blog = buildBlog();
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(blog));
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(aiModerationResultRepository.findWithTagsByBlogIds(any()))
                .thenReturn(List.of());

        RagSnapshotResponseDTO response = service.getSnapshot("blog", null, null, 10);

        assertThat(response.getItems()).hasSize(1);
        var data = response.getItems().get(0).getData();
        assertThat(data).containsKeys("content", "imageUrls", "pageName", "authorUserName", "tags");
        assertThat(data).doesNotContainKeys("userEmail", "userPhone", "userPassword");
        assertThat(response.isHasMore()).isFalse();
    }

    @Test
    void cafePageTombstonesAreReturned() {
        UUID suspendedPageId = UUID.randomUUID();
        when(cafePageRepository.findRagSnapshotCafePages(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(cafePageRepository.findRagTombstoneCafePageIds(any(LocalDateTime.class)))
                .thenReturn(List.of(suspendedPageId));

        RagSnapshotResponseDTO response = service.getSnapshot("cafe_page", LocalDateTime.now(), null, 10);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTombstones()).containsExactly(suspendedPageId.toString());
        assertThat(response.getNextSince()).isNull();
    }

    @Test
    void hasMoreIsTrueWhenLimitPlusOneRowExists() {
        Blog blog = buildBlog();
        Blog nextBlog = buildBlog();
        nextBlog.setId(UUID.randomUUID());
        nextBlog.setCreatedAt(blog.getCreatedAt().plusSeconds(1));
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(blog, nextBlog));
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(aiModerationResultRepository.findWithTagsByBlogIds(any()))
                .thenReturn(List.of());

        RagSnapshotResponseDTO response = service.getSnapshot("blog", null, null, 1);

        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getNextSince()).isEqualTo(blog.getCreatedAt());
        assertThat(response.getNextSourceId()).isEqualTo(blog.getId().toString());
    }

    @Test
    void invalidCursorIdThrowsBadRequest() {
        assertThatThrownBy(() -> service.getSnapshot("blog", null, "not-a-uuid", 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cursorId");
    }

    @Test
    void blogSnapshot_success_attachesModerationTagsAndForeignKeys_TC006() {
        Blog blog = buildBlog();
        blog.setPageId(blog.getPage().getId());
        blog.setRegionId(UUID.randomUUID());
        blog.setImageUrls(List.of("https://cdn.example.com/1.png"));
        blog.setUpdatedAt(LocalDateTime.now());
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(blog));
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class))).thenReturn(List.of());
        when(aiModerationResultRepository.findWithTagsByBlogIds(any()))
                .thenReturn(List.of(
                        moderationResult(blog, List.of("yen tinh", "cafe sua")),
                        // Bản ghi thứ hai cùng blog: chỉ bản đầu được giữ.
                        moderationResult(blog, List.of("bo qua"))));

        RagSnapshotResponseDTO response = service.getSnapshot("blog", null, null, 10);

        var item = response.getItems().get(0);
        assertThat(item.getUpdatedAt()).isEqualTo(blog.getUpdatedAt());
        assertThat(item.getData()).containsEntry("tags", List.of("yen tinh", "cafe sua"));
        assertThat(item.getData()).containsEntry("pageId", blog.getPageId().toString());
        assertThat(item.getData()).containsEntry("regionId", blog.getRegionId().toString());
        assertThat(item.getData()).containsEntry("imageUrls", List.of("https://cdn.example.com/1.png"));
    }

    @Test
    void blogSnapshot_success_blogWithoutPageAuthorOrRegion_TC007() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent("Khong co trang quan");
        blog.setCreatedAt(LocalDateTime.now());
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(blog));
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class))).thenReturn(List.of());
        when(aiModerationResultRepository.findWithTagsByBlogIds(any())).thenReturn(List.of());

        var data = service.getSnapshot("blog", null, null, 10).getItems().get(0).getData();

        assertThat(data.get("pageId")).isNull();
        assertThat(data.get("pageName")).isNull();
        assertThat(data.get("regionId")).isNull();
        assertThat(data.get("authorUserName")).isNull();
        assertThat(data).containsEntry("tags", List.of());
    }

    @Test
    void cafePageSnapshot_success_mapsRegionFields_TC008() {
        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setName("The Hidden Garden");
        page.setAddress("30/4, Ninh Kieu");
        page.setDescription("Quan san vuon");
        page.setAvatarUrl("https://cdn.example.com/avatar.png");
        page.setCoverUrl("https://cdn.example.com/cover.png");
        page.setLikeCount(10);
        page.setFollowerCount(20);
        page.setRegion(region());
        page.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(cafePageRepository.findRagSnapshotCafePages(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(page));
        when(cafePageRepository.findRagTombstoneCafePageIds(any(LocalDateTime.class))).thenReturn(List.of());

        var data = service.getSnapshot("cafe_page", null, null, 10).getItems().get(0).getData();

        assertThat(data).containsEntry("name", "The Hidden Garden");
        assertThat(data).containsEntry("city", "Can Tho");
        assertThat(data).containsEntry("province", "Can Tho");
        assertThat(data).containsEntry("area", "Ninh Kieu");
        assertThat(data).containsEntry("likeCount", 10);
        assertThat(data).containsEntry("followerCount", 20);
    }

    @Test
    void cafePageSnapshot_success_pageWithoutRegion_TC009() {
        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setName("Khong co vung");
        page.setCreatedAt(LocalDateTime.now());
        when(cafePageRepository.findRagSnapshotCafePages(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(page));
        when(cafePageRepository.findRagTombstoneCafePageIds(any(LocalDateTime.class))).thenReturn(List.of());

        var data = service.getSnapshot("cafe_page", null, null, 10).getItems().get(0).getData();

        assertThat(data.get("regionId")).isNull();
        assertThat(data.get("city")).isNull();
    }

    @Test
    void reviewerSnapshot_success_summarisesBadgeHistoryAndMultiplier_TC010() {
        Reviewer reviewer = buildReviewer();
        ReviewerFormula formula = formula();
        when(reviewerRepository.findRagSnapshotReviewers(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(reviewerBadgeHistoryRepository
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(
                        List.of(reviewer.getReviewerId())))
                .thenReturn(List.of(
                        badgeHistory(reviewer, "2026-07", ReviewerBadge.GOLD, 900),
                        badgeHistory(reviewer, "2026-06", ReviewerBadge.GOLD, 850),
                        badgeHistory(reviewer, "2026-05", ReviewerBadge.SILVER, 400)));
        when(reviewerFormulaRepository.findByActiveTrue()).thenReturn(Optional.of(formula));
        when(reviewerRepository.findRagTombstoneReviewerIds(any(LocalDateTime.class))).thenReturn(List.of());

        var data = service.getSnapshot("reviewer", null, null, 10).getItems().get(0).getData();

        assertThat(data).containsEntry("userName", "reviewer_a");
        assertThat(data).containsEntry("latestBadge", "GOLD");
        assertThat(data).containsEntry("latestBadgeMonth", "2026-07");
        assertThat(data).containsEntry("latestBadgeScore", 900L);
        assertThat(data).containsEntry("badgeTotalMonths", 3L);
        assertThat(data).containsEntry("city", "Can Tho");
        assertThat(data).containsEntry("formulaMultiplier", new BigDecimal("2.00"));
        @SuppressWarnings("unchecked")
        Map<String, Long> badgeCounts = (Map<String, Long>) data.get("badgeCounts");
        assertThat(badgeCounts).containsEntry("GOLD", 2L).containsEntry("SILVER", 1L);
    }

    @Test
    void reviewerSnapshot_success_reviewerWithoutHistoryOrUser_TC011() {
        Reviewer reviewer = new Reviewer();
        reviewer.setReviewerId(UUID.randomUUID());
        reviewer.setCreatedAt(LocalDateTime.now());
        when(reviewerRepository.findRagSnapshotReviewers(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of(reviewer));
        when(reviewerBadgeHistoryRepository
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(any()))
                .thenReturn(List.of());
        when(reviewerFormulaRepository.findByActiveTrue()).thenReturn(Optional.empty());
        when(reviewerRepository.findRagTombstoneReviewerIds(any(LocalDateTime.class))).thenReturn(List.of());

        var data = service.getSnapshot("reviewer", null, null, 10).getItems().get(0).getData();

        assertThat(data.get("userName")).isNull();
        assertThat(data.get("latestBadge")).isNull();
        assertThat(data.get("formulaMultiplier")).isNull();
        assertThat(data).containsEntry("followerCount", 0);
        assertThat(data).containsEntry("likeCount", 0);
        assertThat(data).containsEntry("badgeTotalMonths", 0L);
    }

    @Test
    void reviewerSnapshot_success_emptyPageSkipsBadgeQuery_TC012() {
        when(reviewerRepository.findRagSnapshotReviewers(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(reviewerFormulaRepository.findByActiveTrue()).thenReturn(Optional.empty());
        when(reviewerRepository.findRagTombstoneReviewerIds(any(LocalDateTime.class))).thenReturn(List.of());

        assertThat(service.getSnapshot("reviewer", null, null, 10).getItems()).isEmpty();
        verify(reviewerBadgeHistoryRepository, never())
                .findByReviewerReviewerIdInOrderByReviewerReviewerIdAscMonthDesc(any());
    }

    @Test
    void getSnapshot_success_normalizesLimitBounds_TC013() {
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class))).thenReturn(List.of());

        service.getSnapshot("blog", null, null, 0);
        service.getSnapshot("blog", null, null, 5_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(blogRepository, org.mockito.Mockito.times(2))
                .findRagSnapshotBlogs(any(LocalDateTime.class), any(), captor.capture());
        // limit <= 0 rơi về mặc định 200, limit quá lớn bị chặn ở 500; cả hai lấy thêm 1 dòng dò hasMore.
        assertThat(captor.getAllValues().get(0).getPageSize()).isEqualTo(201);
        assertThat(captor.getAllValues().get(1).getPageSize()).isEqualTo(501);
    }

    @Test
    void getSnapshot_success_acceptsValidCursorId_TC014() {
        UUID cursorId = UUID.randomUUID();
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class))).thenReturn(List.of());

        service.getSnapshot("blog", null, "  ", 10);
        service.getSnapshot("blog", null, cursorId.toString(), 10);

        verify(blogRepository).findRagSnapshotBlogs(
                any(LocalDateTime.class), org.mockito.ArgumentMatchers.eq(cursorId), any(Pageable.class));
    }

    @Test
    void formulaData_success_returnsWeightsMultipliersAndThresholds_TC015() {
        ReviewerFormula formula = formula();
        when(reviewerFormulaRepository.findByActiveTrue()).thenReturn(Optional.of(formula));
        when(reviewerBadgeThresholdRepository.findByFormulaIdOrderByMinScoreAsc(formula.getId()))
                .thenReturn(List.of(
                        threshold(ReviewerBadge.IRON, 0),
                        threshold(ReviewerBadge.GOLD, 700)));

        Map<String, Object> data = service.formulaData();

        assertThat(data).containsEntry("formulaId", formula.getId().toString());
        assertThat(data).containsEntry("likeWeight", 1);
        assertThat(data).containsEntry("commentWeight", 5);
        assertThat(data).containsEntry("shareWeight", 3);
        assertThat(data).containsEntry("likePayoutAmount", 100L);
        @SuppressWarnings("unchecked")
        Map<String, Object> multipliers = (Map<String, Object>) data.get("multipliers");
        assertThat(multipliers).containsOnlyKeys("IRON", "BRONZE", "SILVER", "GOLD", "DIAMOND");
        assertThat(multipliers).containsEntry("GOLD", "2.00");
        @SuppressWarnings("unchecked")
        Map<String, Long> thresholds = (Map<String, Long>) data.get("thresholds");
        assertThat(thresholds).containsEntry("IRON", 0L).containsEntry("GOLD", 700L);
    }

    @Test
    void formulaData_success_noActiveFormulaReturnsEmptyMarker_TC016() {
        when(reviewerFormulaRepository.findByActiveTrue()).thenReturn(Optional.empty());

        assertThat(service.formulaData()).containsExactly(entry("empty", true));
    }

    private AiModerationResult moderationResult(Blog blog, List<String> tags) {
        AiModerationResult result = new AiModerationResult();
        result.setId(UUID.randomUUID());
        result.setBlog(blog);
        result.setTags(tags);
        return result;
    }

    private Region region() {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        region.setCity("Can Tho");
        region.setProvince("Can Tho");
        region.setArea("Ninh Kieu");
        return region;
    }

    private Reviewer buildReviewer() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("reviewer_a");
        user.setUserFullName("Nguyen Van A");
        user.setUserAvatar("https://cdn.example.com/a.png");
        user.setUserFollower(30);
        user.setUserLike(12);
        user.setRegion(region());

        Reviewer reviewer = new Reviewer();
        reviewer.setReviewerId(UUID.randomUUID());
        reviewer.setUser(user);
        reviewer.setCreatedAt(LocalDateTime.now().minusMonths(3));
        reviewer.setUpdatedAt(LocalDateTime.now());
        return reviewer;
    }

    private ReviewerBadgeHistory badgeHistory(
            Reviewer reviewer, String month, ReviewerBadge badge, long score) {
        ReviewerBadgeHistory history = new ReviewerBadgeHistory();
        history.setId(UUID.randomUUID());
        history.setReviewer(reviewer);
        history.setMonth(month);
        history.setBadge(badge);
        history.setScore(score);
        return history;
    }

    private ReviewerFormula formula() {
        ReviewerFormula formula = new ReviewerFormula();
        formula.setId(UUID.randomUUID());
        formula.setLikeWeight(1);
        formula.setCommentWeight(5);
        formula.setShareWeight(3);
        formula.setLikePayoutAmount(100L);
        formula.setCommentPayoutAmount(500L);
        formula.setSharePayoutAmount(300L);
        formula.setGoldMultiplier(new BigDecimal("2.00"));
        formula.setUpdatedAt(LocalDateTime.now());
        return formula;
    }

    private ReviewerBadgeThreshold threshold(ReviewerBadge badge, long minScore) {
        ReviewerBadgeThreshold threshold = new ReviewerBadgeThreshold();
        threshold.setId(UUID.randomUUID());
        threshold.setBadge(badge);
        threshold.setMinScore(minScore);
        return threshold;
    }

    private Blog buildBlog() {
        User author = new User();
        author.setUserId(UUID.randomUUID());
        author.setUserName("reviewer_a");

        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setName("The Hidden Garden");

        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(author);
        blog.setPage(page);
        blog.setContent("Quan yen tinh, cafe sua ngon.");
        blog.setLikeCount(3);
        blog.setShareCount(1);
        blog.setCommentCount(2);
        blog.setCreatedAt(LocalDateTime.now().minusDays(1));
        return blog;
    }
}
