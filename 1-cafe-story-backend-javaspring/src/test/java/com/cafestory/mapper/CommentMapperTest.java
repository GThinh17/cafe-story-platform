package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void toCommentResponseDTO_mapsCommentAuthorUserNameFromCommentUser() {
        Blog blog = blog();
        User commentAuthor = user("comment-author");
        Comment parentComment = comment(blog, user("parent-author"), null);
        Comment reply = comment(blog, commentAuthor, parentComment);

        CommentResponseDTO result = commentMapper.toCommentResponseDTO(reply);

        assertThat(result.getId()).isEqualTo(reply.getId());
        assertThat(result.getBlogId()).isEqualTo(blog.getId());
        assertThat(result.getUserId()).isEqualTo(commentAuthor.getUserId());
        assertThat(result.getAuthorUserName()).isEqualTo("comment-author");
        assertThat(result.getParentCommentId()).isEqualTo(parentComment.getId());
        assertThat(result.getContent()).isEqualTo("Comment content");
        assertThat(result.getImageUrls()).containsExactly("https://example.com/comment.png");
        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(result.getCreatedAt()).isEqualTo(reply.getCreatedAt());
    }

    private Blog blog() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent("Blog content");
        return blog;
    }

    private User user(String userName) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(userName);
        return user;
    }

    private Comment comment(Blog blog, User user, Comment parentComment) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBlog(blog);
        comment.setUser(user);
        comment.setParentComment(parentComment);
        comment.setContent("Comment content");
        comment.setImageUrls(List.of("https://example.com/comment.png"));
        comment.setStatus(PostStatus.PUBLISHED);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }
}
