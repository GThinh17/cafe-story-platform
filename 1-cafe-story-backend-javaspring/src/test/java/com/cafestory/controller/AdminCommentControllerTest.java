package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.service.serviceInterface.AdminCommentService;
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
 * Kiểm thử {@link AdminCommentController} — uỷ quyền và chuẩn hoá phân trang.
 */
@ExtendWith(MockitoExtension.class)
class AdminCommentControllerTest {

    @Mock
    private AdminCommentService adminCommentService;

    @InjectMocks
    private AdminCommentController adminCommentController;

    @Test
    void getComments_success_normalizesPaging_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Page<CommentResponseDTO> page = new PageImpl<>(List.of());
        when(adminCommentService.getComments(eq(PostStatus.PUBLISHED), eq(blogId), eq(userId), any(Pageable.class)))
                .thenReturn(page);

        assertThat(adminCommentController.getComments(PostStatus.PUBLISHED, blogId, userId, -2, 250))
                .isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminCommentService).getComments(any(), any(), any(), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getComment_success_delegates_TC002() {
        UUID commentId = UUID.randomUUID();
        CommentResponseDTO response = new CommentResponseDTO();
        when(adminCommentService.getComment(commentId)).thenReturn(response);

        assertThat(adminCommentController.getComment(commentId)).isSameAs(response);
    }

    @Test
    void updateCommentStatus_success_delegates_TC003() {
        UUID commentId = UUID.randomUUID();
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.REMOVED);
        CommentResponseDTO response = new CommentResponseDTO();
        when(adminCommentService.updateCommentStatus(commentId, request)).thenReturn(response);

        assertThat(adminCommentController.updateCommentStatus(commentId, request)).isSameAs(response);
    }

    @Test
    void deleteComment_success_delegates_TC004() {
        UUID commentId = UUID.randomUUID();

        adminCommentController.deleteComment(commentId);

        verify(adminCommentService).deleteComment(commentId);
    }
}
