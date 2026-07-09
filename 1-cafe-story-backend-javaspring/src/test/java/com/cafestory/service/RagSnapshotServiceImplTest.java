package com.cafestory.service;

import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceImplement.RagSnapshotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private RagSnapshotServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RagSnapshotServiceImpl(
                blogRepository, cafePageRepository, reviewerRepository, aiModerationResultRepository,
                reviewerBadgeHistoryRepository, reviewerFormulaRepository);
    }

    @Test
    void unsupportedSourceTypeThrowsBadRequest() {
        assertThatThrownBy(() -> service.getSnapshot("payments", null, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported sourceType");
    }

    @Test
    void blogSnapshotMapsPublicFieldsOnly() {
        Blog blog = buildBlog();
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(blog));
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(aiModerationResultRepository.findWithTagsByBlogIds(any()))
                .thenReturn(List.of());

        RagSnapshotResponseDTO response = service.getSnapshot("blog", null, 10);

        assertThat(response.getItems()).hasSize(1);
        var data = response.getItems().get(0).getData();
        assertThat(data).containsKeys("content", "imageUrls", "pageName", "authorUserName", "tags");
        assertThat(data).doesNotContainKeys("userEmail", "userPhone", "userPassword");
        assertThat(response.isHasMore()).isFalse();
    }

    @Test
    void cafePageTombstonesAreReturned() {
        UUID suspendedPageId = UUID.randomUUID();
        when(cafePageRepository.findRagSnapshotCafePages(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());
        when(cafePageRepository.findRagTombstoneCafePageIds(any(LocalDateTime.class)))
                .thenReturn(List.of(suspendedPageId));

        RagSnapshotResponseDTO response = service.getSnapshot("cafe_page", LocalDateTime.now(), 10);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTombstones()).containsExactly(suspendedPageId.toString());
        assertThat(response.getNextSince()).isNull();
    }

    @Test
    void hasMoreIsTrueWhenPageIsFull() {
        Blog blog = buildBlog();
        when(blogRepository.findRagSnapshotBlogs(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(blog));
        when(blogRepository.findRagTombstoneBlogIds(any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(aiModerationResultRepository.findWithTagsByBlogIds(any()))
                .thenReturn(List.of());

        RagSnapshotResponseDTO response = service.getSnapshot("blog", null, 1);

        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getNextSince()).isEqualTo(blog.getCreatedAt());
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
