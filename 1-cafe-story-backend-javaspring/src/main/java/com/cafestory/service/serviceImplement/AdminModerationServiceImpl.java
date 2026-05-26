package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminModerationResolveRequestDTO;
import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ModerationResolveAction;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.service.serviceInterface.AdminModerationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private static final List<ModerationDecision> QUEUE_DECISIONS =
            List.of(ModerationDecision.NEEDS_REVIEW, ModerationDecision.VIOLATION);

    private final AiModerationResultRepository moderationResultRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;

    public AdminModerationServiceImpl(
            AiModerationResultRepository moderationResultRepository,
            BlogRepository blogRepository,
            CommentRepository commentRepository) {
        this.moderationResultRepository = moderationResultRepository;
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminModerationResultResponseDTO> getQueue(Pageable pageable) {
        return moderationResultRepository.findByDecisionInAndResolvedFalse(QUEUE_DECISIONS, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminModerationResultResponseDTO getResult(UUID resultId) {
        return toResponse(findResult(resultId));
    }

    @Override
    @Transactional
    public AdminModerationResultResponseDTO resolveResult(UUID resultId, AdminModerationResolveRequestDTO request) {
        AiModerationResult result = findResult(resultId);
        PostStatus targetStatus = targetStatus(request.getAction());

        if (result.getBlog() != null) {
            Blog blog = result.getBlog();
            blog.setStatus(targetStatus);
            blogRepository.save(blog);
        } else if (result.getComment() != null) {
            Comment comment = result.getComment();
            comment.setStatus(targetStatus);
            commentRepository.save(comment);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Moderation result has no target content");
        }

        result.setDecision(request.getAction() == ModerationResolveAction.APPROVE
                ? ModerationDecision.SAFE
                : ModerationDecision.VIOLATION);
        result.setResolved(true);
        result.setResolvedAction(request.getAction());
        result.setResolvedAt(LocalDateTime.now());
        return toResponse(moderationResultRepository.save(result));
    }

    private AiModerationResult findResult(UUID resultId) {
        return moderationResultRepository.findById(resultId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation result not found"));
    }

    private PostStatus targetStatus(ModerationResolveAction action) {
        if (action == ModerationResolveAction.APPROVE) {
            return PostStatus.PUBLISHED;
        }
        if (action == ModerationResolveAction.HIDE) {
            return PostStatus.HIDDEN;
        }
        return PostStatus.REMOVED;
    }

    private AdminModerationResultResponseDTO toResponse(AiModerationResult result) {
        AdminModerationResultResponseDTO response = new AdminModerationResultResponseDTO();
        response.setId(result.getId());
        response.setBlogId(result.getBlog() == null ? null : result.getBlog().getId());
        response.setCommentId(result.getComment() == null ? null : result.getComment().getId());
        response.setScore(result.getScore());
        response.setDecision(result.getDecision());
        response.setLabels(result.getLabels());
        response.setExplanation(result.getExplanation());
        response.setModelName(result.getModelName());
        response.setResolved(result.getResolved());
        response.setResolvedAction(result.getResolvedAction());
        response.setResolvedAt(result.getResolvedAt());
        response.setCreatedAt(result.getCreatedAt());
        return response;
    }
}
