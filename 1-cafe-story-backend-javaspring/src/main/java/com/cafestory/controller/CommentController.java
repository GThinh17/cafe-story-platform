package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.requestDTO.CommentUpdateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.service.serviceInterface.CommentService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createComment(
            @Valid @RequestBody CommentCreateDTO commentCreateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        commentCreateDTO.setUserId(requireUserId(principal));
        return commentService.createComment(commentCreateDTO);
    }

    @GetMapping
    public List<CommentResponseDTO> getComments(
            @RequestParam(required = false) UUID blogId,
            @RequestParam(required = false) UUID userId) {
        if (blogId != null) {
            return commentService.getCommentsByBlogId(blogId);
        }
        if (userId != null) {
            return commentService.getCommentsByUserId(userId);
        }
        return commentService.getAllComments();
    }

    @GetMapping("/blogs/{blogId}")
    public List<CommentResponseDTO> getCommentsByBlogId(@PathVariable UUID blogId) {
        return commentService.getCommentsByBlogId(blogId);
    }

    @GetMapping("/users/{userId}")
    public List<CommentResponseDTO> getCommentsByUserId(@PathVariable UUID userId) {
        return commentService.getCommentsByUserId(userId);
    }

    @GetMapping("/{commentId}/replies")
    public List<CommentResponseDTO> getRepliesByCommentId(@PathVariable UUID commentId) {
        return commentService.getRepliesByCommentId(commentId);
    }

    @GetMapping("/{commentId}")
    public CommentResponseDTO getCommentById(@PathVariable UUID commentId) {
        return commentService.getCommentById(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentResponseDTO updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateDTO commentUpdateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return commentService.updateComment(commentId, requireUserId(principal), commentUpdateDTO);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        commentService.deleteComment(commentId, requireUserId(principal));
    }
}
