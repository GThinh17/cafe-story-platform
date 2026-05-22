package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.requestDTO.CommentUpdateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.service.serviceInterface.CommentService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    void createComment_success_TC001() {
        UUID userId = UUID.randomUUID();
        CommentCreateDTO request = createCommentRequest();
        CommentResponseDTO response = commentResponse();

        when(commentService.createComment(request)).thenReturn(response);

        CommentResponseDTO result = commentController.createComment(request, principal(userId));

        assertThat(result).isEqualTo(response);
        assertThat(request.getUserId()).isEqualTo(userId);
        verify(commentService).createComment(request);
    }

    @Test
    void getComments_success_getAllComments_TC002() {
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getAllComments()).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getComments(null, null);

        assertThat(result).isEqualTo(response);
        verify(commentService).getAllComments();
    }

    @Test
    void getComments_success_getCommentsByBlogId_TC003() {
        UUID blogId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByBlogId(blogId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getComments(blogId, null);

        assertThat(result).isEqualTo(response);
        verify(commentService).getCommentsByBlogId(blogId);
    }

    @Test
    void getComments_success_getCommentsByUserId_TC004() {
        UUID userId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByUserId(userId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getComments(null, userId);

        assertThat(result).isEqualTo(response);
        verify(commentService).getCommentsByUserId(userId);
    }

    @Test
    void getCommentsByBlogId_success_TC005() {
        UUID blogId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByBlogId(blogId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getCommentsByBlogId(blogId);

        assertThat(result).isEqualTo(response);
        verify(commentService).getCommentsByBlogId(blogId);
    }

    @Test
    void getCommentsByUserId_success_TC006() {
        UUID userId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByUserId(userId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getCommentsByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(commentService).getCommentsByUserId(userId);
    }

    @Test
    void getRepliesByCommentId_success_TC007() {
        UUID commentId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getRepliesByCommentId(commentId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getRepliesByCommentId(commentId);

        assertThat(result).isEqualTo(response);
        verify(commentService).getRepliesByCommentId(commentId);
    }

    @Test
    void getCommentById_success_TC008() {
        UUID commentId = UUID.randomUUID();
        CommentResponseDTO response = commentResponse();

        when(commentService.getCommentById(commentId)).thenReturn(response);

        CommentResponseDTO result = commentController.getCommentById(commentId);

        assertThat(result).isEqualTo(response);
        verify(commentService).getCommentById(commentId);
    }

    @Test
    void updateComment_success_TC009() {
        UUID commentId = UUID.randomUUID();
        CommentUpdateDTO request = updateCommentRequest();
        CommentResponseDTO response = commentResponse();

        when(commentService.updateComment(commentId, request)).thenReturn(response);

        CommentResponseDTO result = commentController.updateComment(commentId, request);

        assertThat(result).isEqualTo(response);
        verify(commentService).updateComment(commentId, request);
    }

    @Test
    void deleteComment_success_TC010() {
        UUID commentId = UUID.randomUUID();

        commentController.deleteComment(commentId);

        verify(commentService).deleteComment(commentId);
    }

    private CommentCreateDTO createCommentRequest() {
        CommentCreateDTO request = new CommentCreateDTO();
        request.setBlogId(UUID.randomUUID());
        request.setParentCommentId(UUID.randomUUID());
        request.setContent("Comment content");
        request.setImageUrls(List.of("https://example.com/comment-1.png"));
        return request;
    }

    private CommentUpdateDTO updateCommentRequest() {
        CommentUpdateDTO request = new CommentUpdateDTO();
        request.setContent("Updated comment");
        request.setImageUrls(List.of("https://example.com/comment-updated.png"));
        request.setStatus(PostStatus.HIDDEN);
        return request;
    }

    private CommentResponseDTO commentResponse() {
        CommentResponseDTO response = new CommentResponseDTO();
        response.setId(UUID.randomUUID());
        response.setBlogId(UUID.randomUUID());
        response.setUserId(UUID.randomUUID());
        response.setParentCommentId(UUID.randomUUID());
        response.setContent("Comment content");
        response.setImageUrls(List.of("https://example.com/comment-1.png"));
        response.setStatus(PostStatus.PUBLISHED);
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
