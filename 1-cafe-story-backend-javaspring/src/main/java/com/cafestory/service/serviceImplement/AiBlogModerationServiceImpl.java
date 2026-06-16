package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AiBlogModerationRequestDTO;
import com.cafestory.dto.responseDTO.AiBlogModerationResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceInterface.AiBlogModerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiBlogModerationServiceImpl implements AiBlogModerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiBlogModerationServiceImpl.class);
    private static final String MODEL_NAME = "cafestory-python-ai";
    private static final String FALLBACK_REASON = "AI moderation service unavailable. Blog requires admin review.";

    private final AiModerationResultRepository moderationResultRepository;
    private final BlogRepository blogRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiBlogModerationServiceImpl(
            AiModerationResultRepository moderationResultRepository,
            BlogRepository blogRepository,
            ObjectMapper objectMapper,
            @Value("${ai.moderation.base-url:http://localhost:8036}") String baseUrl,
            @Value("${ai.moderation.timeout-ms:10000}") int timeoutMs) {
        this(
                moderationResultRepository,
                blogRepository,
                objectMapper,
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .requestFactory(requestFactory(timeoutMs))
                        .build());
    }

    AiBlogModerationServiceImpl(
            AiModerationResultRepository moderationResultRepository,
            BlogRepository blogRepository,
            ObjectMapper objectMapper,
            RestClient restClient) {
        this.moderationResultRepository = moderationResultRepository;
        this.blogRepository = blogRepository;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @Override
    @Transactional
    public Blog moderateBlog(Blog blog) {
        AiBlogModerationResponseDTO response;
        try {
            response = callAiService(blog);
        } catch (RuntimeException exception) {
            response = fallbackResponse(blog, exception);
        }

        ModerationDecision decision = decision(response.getStatus());
        blog.setStatus(postStatus(decision));
        Blog savedBlog = blogRepository.save(blog);
        moderationResultRepository.save(toModerationResult(savedBlog, response, decision));
        return savedBlog;
    }

    protected AiBlogModerationResponseDTO callAiService(Blog blog) {
        AiBlogModerationRequestDTO request = new AiBlogModerationRequestDTO(
                blog.getId(),
                blog.getContent(),
                blog.getImageUrls() == null ? List.of() : blog.getImageUrls());

        AiModerationRawResponse rawResponse = restClient.post()
                .uri("/api/ai/blogs/evaluate")
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((clientRequest, clientResponse) -> {
                    byte[] responseBytes = clientResponse.getBody().readAllBytes();
                    MediaType contentType = clientResponse.getHeaders().getContentType();
                    return new AiModerationRawResponse(
                            new String(responseBytes, StandardCharsets.UTF_8),
                            contentType == null ? "unknown" : contentType.toString());
                });

        String responseBody = rawResponse.body();
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("AI moderation response is empty");
        }

        try {
            return objectMapper.readValue(responseBody, AiBlogModerationResponseDTO.class);
        } catch (IOException exception) {
            LOGGER.warn(
                    "AI moderation response parse failed contentType={} bodyPreview={}",
                    rawResponse.contentType(),
                    responseBody.substring(0, Math.min(responseBody.length(), 300)));
            throw new IllegalStateException("AI moderation response is not valid JSON", exception);
        }
    }

    private AiModerationResult toModerationResult(
            Blog blog,
            AiBlogModerationResponseDTO response,
            ModerationDecision decision) {
        AiModerationResult result = new AiModerationResult();
        result.setBlog(blog);
        result.setCaption(blog.getContent());
        result.setScore(score(response));
        result.setDecision(decision);
        result.setCaptionScore(response.getCaptionScore());
        result.setCaptionReason(response.getCaptionReason());
        result.setImageScore(response.getImageScore());
        result.setImageReason(response.getImageReason());
        result.setTags(response.getTags() == null ? List.of() : response.getTags());
        result.setAiStatus(normalizeAiStatus(response.getStatus()));
        result.setLabels(String.join(",", result.getTags()));
        result.setExplanation(explanation(response));
        result.setModelName(MODEL_NAME);
        result.setRawResponse(rawResponse(response));
        result.setResolved(decision == ModerationDecision.SAFE);
        return result;
    }

    private AiBlogModerationResponseDTO fallbackResponse(Blog blog, RuntimeException exception) {
        AiBlogModerationResponseDTO response = new AiBlogModerationResponseDTO();
        response.setBlogId(blog.getId());
        response.setCaptionScore(0);
        response.setCaptionReason(FALLBACK_REASON);
        response.setImageScore(0);
        response.setImageReason(exception.getMessage());
        response.setTags(List.of());
        response.setStatus("send Admin");
        return response;
    }

    private Double score(AiBlogModerationResponseDTO response) {
        int captionScore = nullToZero(response.getCaptionScore());
        int imageRiskScore = 100 - nullToZero(response.getImageScore());
        return (double) Math.max(captionScore, imageRiskScore);
    }

    private String explanation(AiBlogModerationResponseDTO response) {
        return "Caption: " + nullToBlank(response.getCaptionReason())
                + " Image: " + nullToBlank(response.getImageReason());
    }

    private Map<String, Object> rawResponse(AiBlogModerationResponseDTO response) {
        return objectMapper.convertValue(response, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    private ModerationDecision decision(String aiStatus) {
        return switch (normalizeAiStatus(aiStatus)) {
            case "APPROVE" -> ModerationDecision.SAFE;
            case "DENY" -> ModerationDecision.VIOLATION;
            default -> ModerationDecision.NEEDS_REVIEW;
        };
    }

    private PostStatus postStatus(ModerationDecision decision) {
        return switch (decision) {
            case SAFE -> PostStatus.PUBLISHED;
            case NEEDS_REVIEW -> PostStatus.HIDDEN;
            case VIOLATION -> PostStatus.REMOVED;
        };
    }

    private String normalizeAiStatus(String aiStatus) {
        if (aiStatus == null || aiStatus.isBlank()) {
            return "SEND_ADMIN";
        }
        String normalized = aiStatus.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        if ("SEND_ADMIN".equals(normalized) || "APPROVE".equals(normalized) || "DENY".equals(normalized)) {
            return normalized;
        }
        return "SEND_ADMIN";
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : Math.max(0, Math.min(100, value));
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        return requestFactory;
    }

    private record AiModerationRawResponse(String body, String contentType) {
    }
}
