package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceImplement.AdminBlogServiceImpl;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminBlogServiceImpl} — bảng quản trị bài viết.
 *
 * <p>Danh sách lấy người được gắn thẻ theo lô để tránh N+1; bài không có ai được
 * gắn thẻ vẫn phải trả danh sách rỗng chứ không phải null.
 */
@ExtendWith(MockitoExtension.class)
class AdminBlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;
    @Mock
    private BlogMapper blogMapper;
    @Mock
    private BlogTagService blogTagService;
    @Mock
    private NotificationService notificationService;

    private AdminBlogServiceImpl adminBlogService;

    private Blog blog;
    private User author;

    @BeforeEach
    void setUp() {
        adminBlogService = new AdminBlogServiceImpl(
                blogRepository, blogMapper, blogTagService, notificationService);
        author = new User();
        author.setUserId(UUID.randomUUID());
        blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent("Quan yen tinh");
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setAuthor(author);
    }

    @Test
    void getBlogs_success_attachesTaggedUsersInBatch_TC001() {
        PageRequest pageable = PageRequest.of(0, 10);
        BlogTaggedUserResponseDTO tagged = new BlogTaggedUserResponseDTO();
        when(blogRepository.findAdminBlogs(PostStatus.PUBLISHED, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(blog)));
        when(blogTagService.getTaggedUsersByBlogIds(List.of(blog.getId())))
                .thenReturn(Map.of(blog.getId(), List.of(tagged)));
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());

        Page<BlogResponseDTO> result =
                adminBlogService.getBlogs(PostStatus.PUBLISHED, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTaggedUsers()).containsExactly(tagged);
    }

    @Test
    void getBlogs_success_blogWithoutTaggedUsersGetsEmptyList_TC002() {
        PageRequest pageable = PageRequest.of(0, 10);
        UUID authorId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        when(blogRepository.findAdminBlogs(null, authorId, pageId, pageable))
                .thenReturn(new PageImpl<>(List.of(blog)));
        when(blogTagService.getTaggedUsersByBlogIds(any())).thenReturn(Map.of());
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());

        assertThat(adminBlogService.getBlogs(null, authorId, pageId, pageable)
                .getContent().get(0).getTaggedUsers()).isEmpty();
    }

    @Test
    void getBlog_success_loadsTaggedUsersForOneBlog_TC003() {
        BlogTaggedUserResponseDTO tagged = new BlogTaggedUserResponseDTO();
        when(blogRepository.findById(blog.getId())).thenReturn(Optional.of(blog));
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());
        when(blogTagService.getTaggedUsers(blog.getId())).thenReturn(List.of(tagged));

        assertThat(adminBlogService.getBlog(blog.getId()).getTaggedUsers()).containsExactly(tagged);
    }

    @Test
    void getBlog_fail_notFound_TC004() {
        UUID blogId = UUID.randomUUID();
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminBlogService.getBlog(blogId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Blog not found");
    }

    @Test
    void updateBlogStatus_success_persistsNewStatus_TC005() {
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.HIDDEN);
        when(blogRepository.findById(blog.getId())).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());
        when(blogTagService.getTaggedUsers(blog.getId())).thenReturn(List.of());

        adminBlogService.updateBlogStatus(blog.getId(), request);

        assertThat(blog.getStatus()).isEqualTo(PostStatus.HIDDEN);
        verify(blogRepository).save(blog);
    }

    /**
     * Admin duyệt tay cũng phải báo cho tác giả. Trước đây chỉ đường AI gửi
     * thông báo kiểm duyệt, nên bài AI đẩy sang admin rồi admin xử tay là tác
     * giả không nhận được gì.
     */
    @Test
    void updateBlogStatus_success_notifiesAuthorWhenHidden_TC010() {
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.HIDDEN);
        request.setReason("  Anh vi pham  ");
        when(blogRepository.findById(blog.getId())).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());
        when(blogTagService.getTaggedUsers(blog.getId())).thenReturn(List.of());

        adminBlogService.updateBlogStatus(blog.getId(), request);

        verify(notificationService).createModerationNotification(
                author.getUserId(), blog.getId(), "DENIED", "Anh vi pham");
    }

    @Test
    void updateBlogStatus_success_notifiesApprovedWhenPublished_TC011() {
        blog.setStatus(PostStatus.HIDDEN);
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.PUBLISHED);
        when(blogRepository.findById(blog.getId())).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());
        when(blogTagService.getTaggedUsers(blog.getId())).thenReturn(List.of());

        adminBlogService.updateBlogStatus(blog.getId(), request);

        verify(notificationService).createModerationNotification(
                author.getUserId(), blog.getId(), "APPROVED", null);
    }

    /** Lưu lại đúng trạng thái cũ không phải quyết định mới — không được spam tác giả. */
    @Test
    void updateBlogStatus_success_skipsNotificationWhenStatusUnchanged_TC012() {
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.PUBLISHED);
        when(blogRepository.findById(blog.getId())).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(new BlogResponseDTO());
        when(blogTagService.getTaggedUsers(blog.getId())).thenReturn(List.of());

        adminBlogService.updateBlogStatus(blog.getId(), request);

        verify(notificationService, never()).createModerationNotification(any(), any(), any(), any());
    }

    @Test
    void deleteBlog_success_removesTagsThenBlog_TC006() {
        when(blogRepository.findById(blog.getId())).thenReturn(Optional.of(blog));

        adminBlogService.deleteBlog(blog.getId());

        verify(blogTagService).deleteBlogTags(blog.getId());
        verify(blogRepository).delete(blog);
    }

    @Test
    void deleteBlog_fail_notFound_TC007() {
        UUID blogId = UUID.randomUUID();
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminBlogService.deleteBlog(blogId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Blog not found");
        verify(blogRepository, never()).delete(any(Blog.class));
    }
}
