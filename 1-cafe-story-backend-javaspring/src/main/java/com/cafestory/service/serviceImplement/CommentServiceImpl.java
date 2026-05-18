package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.requestDTO.CommentUpdateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.User;
import com.cafestory.mapper.CommentMapper;
import com.cafestory.repository.CommentRepository;
import com.cafestory.service.serviceInterface.CommentService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CommentValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final BlogValidator blogValidator;
    private final UserValidator userValidator;
    private final CommentValidator commentValidator;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            CommentMapper commentMapper,
            BlogValidator blogValidator,
            UserValidator userValidator,
            CommentValidator commentValidator) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.blogValidator = blogValidator;
        this.userValidator = userValidator;
        this.commentValidator = commentValidator;
    }

    @Override
    @Transactional
    public CommentResponseDTO createComment(CommentCreateDTO commentCreateDTO) {
        Blog blog = blogValidator.validateBlogExists(commentCreateDTO.getBlogId());
        validateBlogAllowComment(blog);
        User user = userValidator.validateUserExists(commentCreateDTO.getUserId());
        Comment parentComment = validateParentComment(commentCreateDTO.getParentCommentId(), blog.getId());

        Comment comment = commentMapper.toComment(commentCreateDTO);
        comment.setBlog(blog);
        comment.setUser(user);
        comment.setParentComment(parentComment);

        Comment savedComment = commentRepository.save(comment);
        incrementCommentCount(blog);
        return commentMapper.toCommentResponseDTO(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getAllComments() {
        return commentRepository.findAll()
                .stream()
                .map(commentMapper::toCommentResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByBlogId(UUID blogId) {
        blogValidator.validateBlogExists(blogId);
        return commentRepository.findByBlogId(blogId)
                .stream()
                .map(commentMapper::toCommentResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return commentRepository.findByUserUserId(userId)
                .stream()
                .map(commentMapper::toCommentResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getRepliesByCommentId(UUID commentId) {
        commentValidator.validateCommentExists(commentId);
        return commentRepository.findByParentCommentId(commentId)
                .stream()
                .map(commentMapper::toCommentResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDTO getCommentById(UUID commentId) {
        return commentMapper.toCommentResponseDTO(commentValidator.validateCommentExists(commentId));
    }

    @Override
    @Transactional
    public CommentResponseDTO updateComment(UUID commentId, CommentUpdateDTO commentUpdateDTO) {
        Comment comment = commentValidator.validateCommentExists(commentId);

        if (commentUpdateDTO.getContent() != null) {
            comment.setContent(commentUpdateDTO.getContent());
        }
        if (commentUpdateDTO.getImageUrls() != null) {
            comment.setImageUrls(commentUpdateDTO.getImageUrls());
        }
        if (commentUpdateDTO.getStatus() != null) {
            comment.setStatus(commentUpdateDTO.getStatus());
        }

        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toCommentResponseDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentRepository.delete(comment);
        decrementCommentCount(comment.getBlog());
    }

    private void validateBlogAllowComment(Blog blog) {
        if (!Boolean.TRUE.equals(blog.getAllowComment())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Blog does not allow comments");
        }
    }

    private void incrementCommentCount(Blog blog) {
        int currentCount = blog.getCommentCount() == null ? 0 : blog.getCommentCount();
        blog.setCommentCount(currentCount + 1);
    }

    private void decrementCommentCount(Blog blog) {
        int currentCount = blog.getCommentCount() == null ? 0 : blog.getCommentCount();
        blog.setCommentCount(Math.max(0, currentCount - 1));
    }

    private Comment validateParentComment(UUID parentCommentId, UUID blogId) {
        if (parentCommentId == null) {
            return null;
        }

        Comment parentComment = commentValidator.validateCommentExists(parentCommentId);
        if (!parentComment.getBlog().getId().equals(blogId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment must belong to the same blog");
        }
        return parentComment;
    }
}
