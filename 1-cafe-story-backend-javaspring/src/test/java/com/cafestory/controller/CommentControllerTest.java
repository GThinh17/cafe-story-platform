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

import java.time.LocalDateTime;
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
        assertThat(result.getAuthorUserName()).isEqualTo("comment-owner");
        assertThat(request.getUserId()).isEqualTo(userId);
        verify(commentService).createComment(request);
    }

    @Test
    void getComments_success_getAllComments_TC002() {
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getAllComments()).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getComments(null, null);

        assertThat(result).isEqualTo(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("comment-owner");
        verify(commentService).getAllComments();
    }

    @Test
    void getComments_success_getCommentsByBlogId_TC003() {
        UUID blogId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByBlogId(blogId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getComments(blogId, null);

        assertThat(result).isEqualTo(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("comment-owner");
        verify(commentService).getCommentsByBlogId(blogId);
    }

    @Test
    void getComments_success_getCommentsByUserId_TC004() {
        UUID userId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByUserId(userId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getComments(null, userId);

        assertThat(result).isEqualTo(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("comment-owner");
        verify(commentService).getCommentsByUserId(userId);
    }

    @Test
    void getCommentsByBlogId_success_multipleCommentAuthorsAndReplies_TC005() {
        UUID blogId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();
        UUID replyCommentId = UUID.randomUUID();
        UUID nestedReplyUserId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(
                commentResponse(UUID.randomUUID(), blogId, ownerUserId, null, "blog-owner"),
                commentResponse(parentCommentId, blogId, currentUserId, null, "current-user"),
                commentResponse(UUID.randomUUID(), blogId, otherUserId, null, "other-user"),
                commentResponse(replyCommentId, blogId, otherUserId, parentCommentId, "reply-user"),
                commentResponse(UUID.randomUUID(), blogId, nestedReplyUserId, replyCommentId, "nested-reply-user"));

        when(commentService.getCommentsByBlogId(blogId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getCommentsByBlogId(blogId);

        assertThat(result).isEqualTo(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("blog-owner", "current-user", "other-user", "reply-user", "nested-reply-user");
        assertThat(result)
                .extracting(CommentResponseDTO::getId)
                .containsExactlyElementsOf(response.stream().map(CommentResponseDTO::getId).toList());
        assertThat(result)
                .extracting(CommentResponseDTO::getBlogId)
                .containsOnly(blogId);
        assertThat(result)
                .extracting(CommentResponseDTO::getUserId)
                .containsExactly(ownerUserId, currentUserId, otherUserId, otherUserId, nestedReplyUserId);
        assertThat(result.get(3).getParentCommentId()).isEqualTo(parentCommentId);
        assertThat(result.get(4).getParentCommentId()).isEqualTo(replyCommentId);
        assertThat(result)
                .extracting(CommentResponseDTO::getContent)
                .containsOnly("Comment content");
        assertThat(result)
                .extracting(CommentResponseDTO::getCreatedAt)
                .doesNotContainNull();
        verify(commentService).getCommentsByBlogId(blogId);
    }

    @Test
    void getCommentsByUserId_success_TC006() {
        UUID userId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getCommentsByUserId(userId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getCommentsByUserId(userId);

        assertThat(result).isEqualTo(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("comment-owner");
        verify(commentService).getCommentsByUserId(userId);
    }

    @Test
    void getRepliesByCommentId_success_TC007() {
        UUID commentId = UUID.randomUUID();
        List<CommentResponseDTO> response = List.of(commentResponse());

        when(commentService.getRepliesByCommentId(commentId)).thenReturn(response);

        List<CommentResponseDTO> result = commentController.getRepliesByCommentId(commentId);

        assertThat(result).isEqualTo(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("comment-owner");
        verify(commentService).getRepliesByCommentId(commentId);
    }

    @Test
    void getCommentById_success_TC008() {
        UUID commentId = UUID.randomUUID();
        CommentResponseDTO response = commentResponse();

        when(commentService.getCommentById(commentId)).thenReturn(response);

        CommentResponseDTO result = commentController.getCommentById(commentId);

        assertThat(result).isEqualTo(response);
        assertThat(result.getAuthorUserName()).isEqualTo("comment-owner");
        verify(commentService).getCommentById(commentId);
    }

    @Test
    void updateComment_success_TC009() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentUpdateDTO request = updateCommentRequest();
        CommentResponseDTO response = commentResponse();

        when(commentService.updateComment(commentId, userId, request)).thenReturn(response);

        CommentResponseDTO result = commentController.updateComment(commentId, request, principal(userId));

        assertThat(result).isEqualTo(response);
        assertThat(result.getAuthorUserName()).isEqualTo("comment-owner");
        verify(commentService).updateComment(commentId, userId, request);
    }

    @Test
    void deleteComment_success_TC010() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        commentController.deleteComment(commentId, principal(userId));

        verify(commentService).deleteComment(commentId, userId);
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
        return commentResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "comment-owner");
    }

    private CommentResponseDTO commentResponse(
            UUID commentId,
            UUID blogId,
            UUID userId,
            UUID parentCommentId,
            String authorUserName) {
        CommentResponseDTO response = new CommentResponseDTO();
        response.setId(commentId);
        response.setBlogId(blogId);
        response.setUserId(userId);
        response.setAuthorUserName(authorUserName);
        response.setParentCommentId(parentCommentId);
        response.setContent("Comment content");
        response.setImageUrls(List.of("https://example.com/comment-1.png"));
        response.setStatus(PostStatus.PUBLISHED);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
