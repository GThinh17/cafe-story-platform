package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.CommentMapper;
import com.cafestory.repository.CommentRepository;
import com.cafestory.service.serviceImplement.AdminCommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminCommentServiceImpl}.
 *
 * <p>Điểm cần canh là bộ đếm bình luận của bài viết khi admin xoá: phải giảm một
 * đơn vị nhưng không được xuống âm, và bình luận mồ côi (không còn bài) vẫn xoá
 * được.
 */
@ExtendWith(MockitoExtension.class)
class AdminCommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;

    private AdminCommentServiceImpl adminCommentService;

    @BeforeEach
    void setUp() {
        adminCommentService = new AdminCommentServiceImpl(commentRepository, commentMapper);
    }

    @Test
    void getComments_success_mapsPage_TC001() {
        PageRequest pageable = PageRequest.of(0, 10);
        Comment comment = comment(blog(5));
        CommentResponseDTO response = new CommentResponseDTO();
        UUID blogId = comment.getBlog().getId();
        UUID userId = UUID.randomUUID();
        when(commentRepository.findAdminComments(PostStatus.PUBLISHED, blogId, userId, pageable))
                .thenReturn(new PageImpl<>(List.of(comment)));
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(response);

        assertThat(adminCommentService.getComments(PostStatus.PUBLISHED, blogId, userId, pageable).getContent())
                .containsExactly(response);
    }

    @Test
    void getComment_success_mapsSingleComment_TC002() {
        Comment comment = comment(blog(5));
        CommentResponseDTO response = new CommentResponseDTO();
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(response);

        assertThat(adminCommentService.getComment(comment.getId())).isSameAs(response);
    }

    @Test
    void getComment_fail_notFound_TC003() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCommentService.getComment(commentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void updateCommentStatus_success_persistsNewStatus_TC004() {
        Comment comment = comment(blog(5));
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(PostStatus.REMOVED);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(new CommentResponseDTO());

        adminCommentService.updateCommentStatus(comment.getId(), request);

        assertThat(comment.getStatus()).isEqualTo(PostStatus.REMOVED);
    }

    @Test
    void deleteComment_success_decrementsBlogCounter_TC005() {
        Blog blog = blog(5);
        Comment comment = comment(blog);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        adminCommentService.deleteComment(comment.getId());

        verify(commentRepository).delete(comment);
        assertThat(blog.getCommentCount()).isEqualTo(4);
    }

    @Test
    void deleteComment_success_counterNeverGoesNegative_TC006() {
        Blog blog = blog(0);
        Comment comment = comment(blog);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        adminCommentService.deleteComment(comment.getId());

        assertThat(blog.getCommentCount()).isZero();
    }

    @Test
    void deleteComment_success_nullCounterIsTreatedAsZero_TC007() {
        Blog blog = blog(null);
        Comment comment = comment(blog);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        adminCommentService.deleteComment(comment.getId());

        assertThat(blog.getCommentCount()).isZero();
    }

    @Test
    void deleteComment_success_commentWithoutBlogIsStillRemoved_TC008() {
        Comment comment = comment(null);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        adminCommentService.deleteComment(comment.getId());

        verify(commentRepository).delete(comment);
    }

    private Blog blog(Integer commentCount) {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setCommentCount(commentCount);
        return blog;
    }

    private Comment comment(Blog blog) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBlog(blog);
        comment.setStatus(PostStatus.PUBLISHED);
        return comment;
    }
}
