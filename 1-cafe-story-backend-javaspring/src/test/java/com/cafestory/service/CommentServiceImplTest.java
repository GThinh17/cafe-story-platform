package com.cafestory.service;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.requestDTO.CommentUpdateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.CommentMapper;
import com.cafestory.repository.CommentRepository;
import com.cafestory.service.model.ActorContext;
import com.cafestory.service.serviceImplement.CommentServiceImpl;
import com.cafestory.validation.ActorContextResolver;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.CommentValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private ActorContextResolver actorContextResolver;

    @Mock
    private CafePageValidator cafePageValidator;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void createComment_success_replyWithImages_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();
        CommentCreateDTO request = createCommentRequest(blogId, userId, parentCommentId);
        Blog blog = blog(blogId);
        User user = user(userId);
        Comment parentComment = comment(parentCommentId, blog, user);
        Comment comment = comment(UUID.randomUUID(), blog, user);
        Comment savedComment = comment(UUID.randomUUID(), blog, user);
        CommentResponseDTO response = commentResponse(savedComment.getId(), blogId, userId, parentCommentId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, null, null))
                .thenReturn(new ActorContext(user, ActorContextType.USER, null));
        when(commentValidator.validateCommentExists(parentCommentId)).thenReturn(parentComment);
        when(commentMapper.toComment(request)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(commentMapper.toCommentResponseDTO(savedComment)).thenReturn(response);

        CommentResponseDTO result = commentService.createComment(request);

        assertThat(result).isEqualTo(response);
        assertThat(result.getAuthorUserName()).isEqualTo(user.getUserName());
        assertThat(comment.getBlog()).isEqualTo(blog);
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(comment.getParentComment()).isEqualTo(parentComment);
        assertThat(blog.getCommentCount()).isEqualTo(1);
        verify(commentRepository).save(comment);
    }

    @Test
    void createComment_fail_blogDoesNotAllowComment_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateDTO request = createCommentRequest(blogId, userId, null);
        Blog blog = blog(blogId);
        blog.setAllowComment(false);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog does not allow comments"));

        verify(userValidator, never()).validateUserExists(userId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_fail_parentCommentDifferentBlog_TC003() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();
        CommentCreateDTO request = createCommentRequest(blogId, userId, parentCommentId);
        Blog blog = blog(blogId);
        Comment parentComment = comment(parentCommentId, blog(UUID.randomUUID()), user(userId));

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, null, null))
                .thenReturn(new ActorContext(user(userId), ActorContextType.USER, null));
        when(commentValidator.validateCommentExists(parentCommentId)).thenReturn(parentComment);

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Parent comment must belong to the same blog"));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getAllComments_success_TC004() {
        Blog blog = blog(UUID.randomUUID());
        User user = user(UUID.randomUUID());
        Comment comment = comment(UUID.randomUUID(), blog, user);
        CommentResponseDTO response = commentResponse(comment.getId(), blog.getId(), user.getUserId(), null, user.getUserName());

        when(commentRepository.findAll()).thenReturn(List.of(comment));
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(response);

        List<CommentResponseDTO> result = commentService.getAllComments();

        assertThat(result).containsExactly(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly(user.getUserName());
    }

    @Test
    void getCommentsByBlogId_success_multipleCommentAuthorsAndReplies_TC005() {
        UUID blogId = UUID.randomUUID();
        Blog blog = blog(blogId);
        User owner = user(UUID.randomUUID(), "blog-owner");
        User currentUser = user(UUID.randomUUID(), "current-user");
        User otherUser = user(UUID.randomUUID(), "other-user");
        User replyUser = user(UUID.randomUUID(), "reply-user");
        User nestedReplyUser = user(UUID.randomUUID(), "nested-reply-user");
        Comment ownerComment = comment(UUID.randomUUID(), blog, owner);
        Comment currentUserComment = comment(UUID.randomUUID(), blog, currentUser);
        Comment otherUserComment = comment(UUID.randomUUID(), blog, otherUser);
        Comment reply = comment(UUID.randomUUID(), blog, replyUser);
        reply.setParentComment(currentUserComment);
        Comment nestedReply = comment(UUID.randomUUID(), blog, nestedReplyUser);
        nestedReply.setParentComment(reply);
        CommentResponseDTO ownerResponse = commentResponse(ownerComment.getId(), blogId, owner.getUserId(), null, owner.getUserName());
        CommentResponseDTO currentUserResponse = commentResponse(
                currentUserComment.getId(),
                blogId,
                currentUser.getUserId(),
                null,
                currentUser.getUserName());
        CommentResponseDTO otherUserResponse = commentResponse(
                otherUserComment.getId(),
                blogId,
                otherUser.getUserId(),
                null,
                otherUser.getUserName());
        CommentResponseDTO replyResponse = commentResponse(
                reply.getId(),
                blogId,
                replyUser.getUserId(),
                currentUserComment.getId(),
                replyUser.getUserName());
        CommentResponseDTO nestedReplyResponse = commentResponse(
                nestedReply.getId(),
                blogId,
                nestedReplyUser.getUserId(),
                reply.getId(),
                nestedReplyUser.getUserName());

        when(commentRepository.findByBlogId(blogId))
                .thenReturn(List.of(ownerComment, currentUserComment, otherUserComment, reply, nestedReply));
        when(commentMapper.toCommentResponseDTO(ownerComment)).thenReturn(ownerResponse);
        when(commentMapper.toCommentResponseDTO(currentUserComment)).thenReturn(currentUserResponse);
        when(commentMapper.toCommentResponseDTO(otherUserComment)).thenReturn(otherUserResponse);
        when(commentMapper.toCommentResponseDTO(reply)).thenReturn(replyResponse);
        when(commentMapper.toCommentResponseDTO(nestedReply)).thenReturn(nestedReplyResponse);

        List<CommentResponseDTO> result = commentService.getCommentsByBlogId(blogId);

        assertThat(result).containsExactly(ownerResponse, currentUserResponse, otherUserResponse, replyResponse, nestedReplyResponse);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly("blog-owner", "current-user", "other-user", "reply-user", "nested-reply-user");
        assertThat(result)
                .extracting(CommentResponseDTO::getId)
                .containsExactly(
                        ownerComment.getId(),
                        currentUserComment.getId(),
                        otherUserComment.getId(),
                        reply.getId(),
                        nestedReply.getId());
        assertThat(result)
                .extracting(CommentResponseDTO::getBlogId)
                .containsOnly(blogId);
        assertThat(result)
                .extracting(CommentResponseDTO::getUserId)
                .containsExactly(
                        owner.getUserId(),
                        currentUser.getUserId(),
                        otherUser.getUserId(),
                        replyUser.getUserId(),
                        nestedReplyUser.getUserId());
        assertThat(result.get(3).getParentCommentId()).isEqualTo(currentUserComment.getId());
        assertThat(result.get(4).getParentCommentId()).isEqualTo(reply.getId());
        assertThat(result)
                .extracting(CommentResponseDTO::getContent)
                .containsOnly("Comment content");
        assertThat(result)
                .extracting(CommentResponseDTO::getCreatedAt)
                .doesNotContainNull();
        verify(blogValidator).validateBlogExists(blogId);
    }

    @Test
    void getCommentsByUserId_success_TC006() {
        UUID userId = UUID.randomUUID();
        Comment comment = comment(UUID.randomUUID(), blog(UUID.randomUUID()), user(userId));
        CommentResponseDTO response = commentResponse(comment.getId(), UUID.randomUUID(), userId, null);

        when(commentRepository.findByUserUserId(userId)).thenReturn(List.of(comment));
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(response);

        List<CommentResponseDTO> result = commentService.getCommentsByUserId(userId);

        assertThat(result).containsExactly(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly(comment.getUser().getUserName());
        verify(userValidator).validateUserExists(userId);
    }

    @Test
    void getRepliesByCommentId_success_TC007() {
        UUID parentCommentId = UUID.randomUUID();
        Blog blog = blog(UUID.randomUUID());
        User user = user(UUID.randomUUID());
        Comment parentComment = comment(parentCommentId, blog, user);
        Comment reply = comment(UUID.randomUUID(), blog, user);
        reply.setParentComment(parentComment);
        CommentResponseDTO response = commentResponse(reply.getId(), blog.getId(), user.getUserId(), parentCommentId);

        when(commentValidator.validateCommentExists(parentCommentId)).thenReturn(parentComment);
        when(commentRepository.findByParentCommentId(parentCommentId)).thenReturn(List.of(reply));
        when(commentMapper.toCommentResponseDTO(reply)).thenReturn(response);

        List<CommentResponseDTO> result = commentService.getRepliesByCommentId(parentCommentId);

        assertThat(result).containsExactly(response);
        assertThat(result)
                .extracting(CommentResponseDTO::getAuthorUserName)
                .containsExactly(user.getUserName());
    }

    @Test
    void getCommentById_success_TC008() {
        UUID commentId = UUID.randomUUID();
        Comment comment = comment(commentId, blog(UUID.randomUUID()), user(UUID.randomUUID()));
        CommentResponseDTO response = commentResponse(commentId, comment.getBlog().getId(), comment.getUser().getUserId(), null);

        when(commentValidator.validateCommentExists(commentId)).thenReturn(comment);
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(response);

        CommentResponseDTO result = commentService.getCommentById(commentId);

        assertThat(result).isEqualTo(response);
        assertThat(result.getAuthorUserName()).isEqualTo(comment.getUser().getUserName());
    }

    @Test
    void updateComment_success_updateAllFields_TC009() {
        UUID commentId = UUID.randomUUID();
        CommentUpdateDTO request = updateCommentRequest();
        Comment comment = comment(commentId, blog(UUID.randomUUID()), user(UUID.randomUUID()));
        UUID actorUserId = comment.getUser().getUserId();
        CommentResponseDTO response = commentResponse(commentId, comment.getBlog().getId(), comment.getUser().getUserId(), null);

        when(commentValidator.validateCommentExists(commentId)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentResponseDTO(comment)).thenReturn(response);

        CommentResponseDTO result = commentService.updateComment(commentId, actorUserId, request);

        assertThat(result).isEqualTo(response);
        assertThat(result.getAuthorUserName()).isEqualTo(comment.getUser().getUserName());
        assertThat(comment.getContent()).isEqualTo("Updated comment");
        assertThat(comment.getImageUrls()).containsExactly("https://example.com/comment-updated.png");
        assertThat(comment.getStatus()).isEqualTo(PostStatus.HIDDEN);
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteComment_success_TC010() {
        UUID commentId = UUID.randomUUID();
        Blog blog = blog(UUID.randomUUID());
        blog.setCommentCount(2);
        Comment comment = comment(commentId, blog, user(UUID.randomUUID()));
        UUID actorUserId = comment.getUser().getUserId();

        when(commentValidator.validateCommentExists(commentId)).thenReturn(comment);

        commentService.deleteComment(commentId, actorUserId);

        assertThat(blog.getCommentCount()).isEqualTo(1);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_success_commentCountCannotBeNegative_TC011() {
        UUID commentId = UUID.randomUUID();
        Blog blog = blog(UUID.randomUUID());
        blog.setCommentCount(0);
        Comment comment = comment(commentId, blog, user(UUID.randomUUID()));
        UUID actorUserId = comment.getUser().getUserId();

        when(commentValidator.validateCommentExists(commentId)).thenReturn(comment);

        commentService.deleteComment(commentId, actorUserId);

        assertThat(blog.getCommentCount()).isZero();
        verify(commentRepository).delete(comment);
    }

    private CommentCreateDTO createCommentRequest(UUID blogId, UUID userId, UUID parentCommentId) {
        CommentCreateDTO request = new CommentCreateDTO();
        request.setBlogId(blogId);
        request.setUserId(userId);
        request.setParentCommentId(parentCommentId);
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

    private Blog blog(UUID blogId) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setContent("Blog content");
        blog.setAllowComment(true);
        blog.setCommentCount(0);
        return blog;
    }

    private User user(UUID userId) {
        return user(userId, "luan123");
    }

    private User user(UUID userId, String userName) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setUserPassword("123456");
        user.setUserEmail(userName + "@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private Comment comment(UUID commentId, Blog blog, User user) {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setBlog(blog);
        comment.setUser(user);
        comment.setContent("Comment content");
        comment.setImageUrls(List.of("https://example.com/comment-1.png"));
        comment.setStatus(PostStatus.PUBLISHED);
        return comment;
    }

    private CommentResponseDTO commentResponse(UUID commentId, UUID blogId, UUID userId, UUID parentCommentId) {
        return commentResponse(commentId, blogId, userId, parentCommentId, "luan123");
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
}
