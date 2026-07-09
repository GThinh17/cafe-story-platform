package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.RagBlogModerationItemResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.service.serviceInterface.RagUserContextService;
import com.cafestory.until.security.JwtClaims;
import com.cafestory.until.security.JwtService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RagUserContextServiceImpl implements RagUserContextService {

    private static final int MAX_LIMIT = 10;
    private static final int DEFAULT_LIMIT = 5;
    private static final int CAPTION_SNIPPET_LENGTH = 120;

    private final JwtService jwtService;
    private final AiModerationResultRepository aiModerationResultRepository;

    public RagUserContextServiceImpl(
            JwtService jwtService, AiModerationResultRepository aiModerationResultRepository) {
        this.jwtService = jwtService;
        this.aiModerationResultRepository = aiModerationResultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RagBlogModerationItemResponseDTO> getBlogModerationForUser(String userJwt, Integer limit) {
        JwtClaims claims;
        try {
            claims = jwtService.validateAccessToken(userJwt);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user token");
        }

        int normalizedLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        List<AiModerationResult> results = aiModerationResultRepository.findRagUserModerationResults(
                claims.userId(), PageRequest.of(0, normalizedLimit));

        // Chỉ map field an toàn cho chatbot — KHÔNG expose rawResponse/labels/score nội bộ.
        return results.stream().map(this::toItem).toList();
    }

    private RagBlogModerationItemResponseDTO toItem(AiModerationResult result) {
        String caption = result.getCaption();
        String captionSnippet = caption == null ? null
                : caption.length() <= CAPTION_SNIPPET_LENGTH
                        ? caption
                        : caption.substring(0, CAPTION_SNIPPET_LENGTH) + "…";
        return new RagBlogModerationItemResponseDTO(
                result.getBlog().getId().toString(),
                result.getBlog().getStatus() == null ? null : result.getBlog().getStatus().name(),
                result.getDecision() == null ? null : result.getDecision().name(),
                result.getCaptionReason(),
                result.getImageReason(),
                captionSnippet,
                result.getResolved(),
                result.getResolvedAction() == null ? null : result.getResolvedAction().name(),
                result.getCreatedAt());
    }
}
