package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminModerationResolveRequestDTO;
import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ModerationResolveAction;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceInterface.AdminModerationService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private static final List<ModerationDecision> QUEUE_DECISIONS =
            List.of(ModerationDecision.NEEDS_REVIEW, ModerationDecision.VIOLATION);

    private final AiModerationResultRepository moderationResultRepository;
    private final BlogRepository blogRepository;

    public AdminModerationServiceImpl(
            AiModerationResultRepository moderationResultRepository,
            BlogRepository blogRepository) {
        this.moderationResultRepository = moderationResultRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminModerationResultResponseDTO> getAllResults(
            String aiStatus,
            ModerationDecision decision,
            Boolean resolved,
            Pageable pageable) {
        Specification<AiModerationResult> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (aiStatus != null && !aiStatus.isBlank()) {
                predicates.add(cb.equal(root.get("aiStatus"), aiStatus));
            }
            if (decision != null) {
                predicates.add(cb.equal(root.get("decision"), decision));
            }
            if (resolved != null) {
                predicates.add(cb.equal(root.get("resolved"), resolved));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        return moderationResultRepository.findAll(spec, pageable)
                .map(this::toResponse);
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

        Blog blog = result.getBlog();
        if (blog == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Moderation result has no target blog");
        }
        blog.setStatus(targetStatus);
        blogRepository.save(blog);

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
        Blog blog = result.getBlog();
        response.setBlogId(blog == null ? null : blog.getId());
        if (blog != null) {
            response.setBlogStatus(blog.getStatus());
            if (blog.getAuthor() != null) {
                response.setAuthorUserId(blog.getAuthor().getUserId());
                response.setAuthorUserName(blog.getAuthor().getUserName());
                response.setAuthorUserFullName(blog.getAuthor().getUserFullName());
                response.setAuthorUserAvatar(blog.getAuthor().getUserAvatar());
            }
        }
        response.setCaption(result.getCaption());
        response.setScore(result.getScore());
        response.setDecision(result.getDecision());
        response.setCaptionScore(result.getCaptionScore());
        response.setCaptionReason(result.getCaptionReason());
        response.setImageScore(result.getImageScore());
        response.setImageReason(result.getImageReason());
        response.setTags(result.getTags());
        response.setAiStatus(result.getAiStatus());
        response.setLabels(result.getLabels());
        response.setExplanation(result.getExplanation());
        response.setModelName(result.getModelName());
        response.setResolved(result.getResolved());
        response.setResolvedAction(result.getResolvedAction());
        response.setResolvedAt(result.getResolvedAt());
        response.setCreatedAt(result.getCreatedAt());
        response.setUpdatedAt(result.getUpdatedAt());
        return response;
    }
}
