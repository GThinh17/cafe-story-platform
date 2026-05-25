package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.CommentMapper;
import com.cafestory.repository.CommentRepository;
import com.cafestory.service.serviceInterface.AdminCommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public AdminCommentServiceImpl(CommentRepository commentRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getComments(PostStatus status, UUID blogId, UUID userId, Pageable pageable) {
        return commentRepository.findAdminComments(status, blogId, userId, pageable)
                .map(commentMapper::toCommentResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDTO getComment(UUID commentId) {
        return commentMapper.toCommentResponseDTO(findComment(commentId));
    }

    @Override
    @Transactional
    public CommentResponseDTO updateCommentStatus(UUID commentId, AdminPostStatusUpdateRequestDTO request) {
        Comment comment = findComment(commentId);
        comment.setStatus(request.getStatus());
        return commentMapper.toCommentResponseDTO(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = findComment(commentId);
        Blog blog = comment.getBlog();
        commentRepository.delete(comment);
        if (blog != null) {
            int currentCount = blog.getCommentCount() == null ? 0 : blog.getCommentCount();
            blog.setCommentCount(Math.max(0, currentCount - 1));
        }
    }

    private Comment findComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }
}
