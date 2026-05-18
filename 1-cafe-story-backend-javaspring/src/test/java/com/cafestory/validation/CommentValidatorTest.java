package com.cafestory.validation;

import com.cafestory.entity.Comment;
import com.cafestory.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentValidatorTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentValidator commentValidator;

    @Test
    void validateCommentExists_success_TC001() {
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment();
        comment.setId(commentId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Comment result = commentValidator.validateCommentExists(commentId);

        assertThat(result).isEqualTo(comment);
    }

    @Test
    void validateCommentExists_fail_nullCommentId_TC002() {
        assertThatThrownBy(() -> commentValidator.validateCommentExists(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Comment id is required"));

        verify(commentRepository, never()).findById(null);
    }

    @Test
    void validateCommentExists_fail_commentNotFound_TC003() {
        UUID commentId = UUID.randomUUID();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentValidator.validateCommentExists(commentId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Comment not found"));
    }
}
