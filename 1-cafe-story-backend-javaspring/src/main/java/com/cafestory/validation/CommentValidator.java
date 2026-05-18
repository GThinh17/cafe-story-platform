package com.cafestory.validation;

import com.cafestory.entity.Comment;
import com.cafestory.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CommentValidator {

    private final CommentRepository commentRepository;

    public CommentValidator(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment validateCommentExists(UUID commentId) {
        validateCommentIdRequired(commentId);
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private void validateCommentIdRequired(UUID commentId) {
        if (commentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment id is required");
        }
    }
}
