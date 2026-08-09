package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.service.serviceInterface.AdminBlogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminBlogController}.
 *
 * <p>Bộ điều khiển chỉ uỷ quyền cho service; phần logic riêng là chuẩn hoá phân
 * trang — trang âm về 0, kích thước kẹp trong khoảng 1–100, luôn sắp xếp theo
 * thời gian tạo giảm dần.
 */
@ExtendWith(MockitoExtension.class)
class AdminBlogControllerTest {

    @Mock
    private AdminBlogService adminBlogService;

    @InjectMocks
    private AdminBlogController adminBlogController;

    @Test
    void getBlogs_success_normalizesPaging_TC001() {
        UUID authorUserId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        Page<BlogResponseDTO> page = new PageImpl<>(List.of());
        when(adminBlogService.getBlogs(eq(PostStatus.PUBLISHED), eq(authorUserId), eq(pageId), any(Pageable.class)))
                .thenReturn(page);

        assertThat(adminBlogController.getBlogs(PostStatus.PUBLISHED, authorUserId, pageId, -5, 500))
                .isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminBlogService).getBlogs(any(), any(), any(), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getBlogs_success_zeroSizeBecomesOne_TC002() {
        when(adminBlogService.getBlogs(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adminBlogController.getBlogs(null, null, null, 2, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminBlogService).getBlogs(any(), any(), any(), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    void getBlog_success_delegates_TC003() {
        UUID blogId = UUID.randomUUID();
        BlogResponseDTO response = new BlogResponseDTO();
        when(adminBlogService.getBlog(blogId)).thenReturn(response);

        assertThat(adminBlogController.getBlog(blogId)).isSameAs(response);
    }

    @Test
    void updateBlogStatus_success_delegates_TC004() {
        UUID blogId = UUID.randomUUID();
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.HIDDEN);
        BlogResponseDTO response = new BlogResponseDTO();
        when(adminBlogService.updateBlogStatus(blogId, request)).thenReturn(response);

        assertThat(adminBlogController.updateBlogStatus(blogId, request)).isSameAs(response);
    }

    @Test
    void deleteBlog_success_delegates_TC005() {
        UUID blogId = UUID.randomUUID();

        adminBlogController.deleteBlog(blogId);

        verify(adminBlogService).deleteBlog(blogId);
    }
}
