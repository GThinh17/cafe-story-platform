package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminCommentService {

    Page<CommentResponseDTO> getComments(PostStatus status, UUID blogId, UUID userId, Pageable pageable);

    CommentResponseDTO getComment(UUID commentId);

    CommentResponseDTO updateCommentStatus(UUID commentId, AdminPostStatusUpdateRequestDTO request);

    void deleteComment(UUID commentId);
}
