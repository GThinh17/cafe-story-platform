package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.requestDTO.CommentUpdateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponseDTO createComment(CommentCreateDTO commentCreateDTO);

    List<CommentResponseDTO> getAllComments();

    List<CommentResponseDTO> getCommentsByBlogId(UUID blogId);

    List<CommentResponseDTO> getCommentsByUserId(UUID userId);

    List<CommentResponseDTO> getRepliesByCommentId(UUID commentId);

    CommentResponseDTO getCommentById(UUID commentId);

    CommentResponseDTO updateComment(UUID commentId, CommentUpdateDTO commentUpdateDTO);

    void deleteComment(UUID commentId);
}
