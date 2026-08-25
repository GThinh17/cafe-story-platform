package com.cafestory.service.serviceImplement;

import com.cafestory.config.AdminAssistantSecurityProperties;
import com.cafestory.dto.requestDTO.AdminAssistantConversationCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminAssistantMessageRequestDTO;
import com.cafestory.dto.requestDTO.AdminCafePageStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminUserStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantChatResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantConversationResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantDraftActionResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantMessageResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantWebhookResponseDTO;
import com.cafestory.entity.AdminAssistantConversation;
import com.cafestory.entity.AdminAssistantDraftAction;
import com.cafestory.entity.AdminAssistantMessage;
import com.cafestory.entity.enums.AdminAssistantDraftActionStatus;
import com.cafestory.entity.enums.AdminAssistantDraftActionType;
import com.cafestory.entity.enums.AdminAssistantMessageRole;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.repository.AdminAssistantConversationRepository;
import com.cafestory.repository.AdminAssistantDraftActionRepository;
import com.cafestory.repository.AdminAssistantMessageRepository;
import com.cafestory.service.serviceInterface.AdminAssistantService;
import com.cafestory.service.serviceInterface.AdminBlogService;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import com.cafestory.service.serviceInterface.AdminCommentService;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.cafestory.service.serviceInterface.AdminUserService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AdminAssistantServiceImpl implements AdminAssistantService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final int DRAFT_EXPIRE_MINUTES = 15;

    private final AdminAssistantConversationRepository conversationRepository;
    private final AdminAssistantMessageRepository messageRepository;
    private final AdminAssistantDraftActionRepository draftActionRepository;
    private final ContentReportService contentReportService;
    private final AdminReportAiResolutionService reportAiResolutionService;
    private final AdminReportAiAutoApplyJobService autoApplyJobService;
    private final AdminBlogService adminBlogService;
    private final AdminCommentService adminCommentService;
    private final AdminUserService adminUserService;
    private final AdminCafePageService adminCafePageService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String assistantModel;
    private final String toolBaseUrl;
    private final String toolToken;

    public AdminAssistantServiceImpl(
            AdminAssistantConversationRepository conversationRepository,
            AdminAssistantMessageRepository messageRepository,
            AdminAssistantDraftActionRepository draftActionRepository,
            ContentReportService contentReportService,
            AdminReportAiResolutionService reportAiResolutionService,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            AdminBlogService adminBlogService,
            AdminCommentService adminCommentService,
            AdminUserService adminUserService,
            AdminCafePageService adminCafePageService,
            ObjectMapper objectMapper,
            @Value("${admin.assistant.webhook-url:http://localhost:5678/webhook/cafestory-admin-assistant-chat}")
            String webhookUrl,
            @Value("${admin.assistant.timeout-ms:90000}") int timeoutMs,
            @Value("${admin.assistant.model:gpt-4o-mini}") String assistantModel,
            @Value("${admin.assistant.tool-base-url:http://host.docker.internal:8080/api/admin/assistant/tools}") String toolBaseUrl,
            AdminAssistantSecurityProperties securityProperties) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.draftActionRepository = draftActionRepository;
        this.contentReportService = contentReportService;
        this.reportAiResolutionService = reportAiResolutionService;
        this.autoApplyJobService = autoApplyJobService;
        this.adminBlogService = adminBlogService;
        this.adminCommentService = adminCommentService;
        this.adminUserService = adminUserService;
        this.adminCafePageService = adminCafePageService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(webhookUrl)
                .requestFactory(requestFactory(timeoutMs))
                .build();
        this.assistantModel = assistantModel;
        this.toolBaseUrl = toolBaseUrl;
        this.toolToken = securityProperties.getToolToken();
    }

    @Override
    @Transactional
    public AdminAssistantConversationResponseDTO createConversation(
            UUID adminUserId,
            AdminAssistantConversationCreateRequestDTO request) {
        AdminAssistantConversation conversation = new AdminAssistantConversation();
        conversation.setAdminUserId(requireAdminUserId(adminUserId));
        conversation.setTitle(normalizeTitle(request == null ? null : request.getTitle()));
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAssistantConversationResponseDTO> getConversations(UUID adminUserId, Pageable pageable) {
        return conversationRepository.findByAdminUserId(requireAdminUserId(adminUserId), pageable)
                .map(this::toConversationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAssistantMessageResponseDTO> getMessages(UUID adminUserId, UUID conversationId, Pageable pageable) {
        AdminAssistantConversation conversation = findConversation(adminUserId, conversationId);
        return messageRepository.findByConversationId(conversation.getId(), pageable)
                .map(this::toMessageResponse);
    }

    @Override
    @Transactional
    public AdminAssistantChatResponseDTO sendMessage(
            UUID adminUserId,
            UUID conversationId,
            AdminAssistantMessageRequestDTO request) {
        AdminAssistantConversation conversation = findConversation(adminUserId, conversationId);
        String userText = normalizeMessage(request);
        AdminAssistantMessage userMessage = saveMessage(conversation, AdminAssistantMessageRole.USER, userText,
                Map.of("pageContext", request.getPageContext() == null ? Map.of() : request.getPageContext()));
        AdminAssistantWebhookResponseDTO webhookResponse = callWebhook(conversation, userMessage, request.getPageContext());
        String answer = webhookResponse.getAnswer() == null || webhookResponse.getAnswer().isBlank()
                ? "I could not produce an admin assistant response."
                : webhookResponse.getAnswer().trim();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("modelName", webhookResponse.getModelName() == null ? assistantModel : webhookResponse.getModelName());
        metadata.put("citations", safeList(webhookResponse.getCitations()));
        metadata.put("toolCalls", safeList(webhookResponse.getToolCalls()));
        AdminAssistantMessage assistantMessage = saveMessage(conversation, AdminAssistantMessageRole.ASSISTANT, answer, metadata);
        AdminAssistantDraftAction draftAction = createDraftActionIfValid(
                conversation,
                assistantMessage,
                adminUserId,
                webhookResponse.getDraftAction());

        AdminAssistantChatResponseDTO response = new AdminAssistantChatResponseDTO();
        response.setMessage(toMessageResponse(assistantMessage));
        response.setDraftAction(draftAction == null ? null : toDraftActionResponse(draftAction));
        response.setCitations(safeList(webhookResponse.getCitations()));
        response.setToolCalls(safeList(webhookResponse.getToolCalls()));
        return response;
    }

    @Override
    public SseEmitter streamMessage(UUID adminUserId, UUID conversationId, AdminAssistantMessageRequestDTO request) {
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("progress").data(Map.of("message", "Assistant request accepted.")));
                AdminAssistantChatResponseDTO response = sendMessage(adminUserId, conversationId, request);
                emitter.send(SseEmitter.event().name("message").data(response));
                emitter.send(SseEmitter.event().name("done").data(Map.of("ok", true)));
                emitter.complete();
            } catch (Exception exception) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of(
                            "message",
                            exception.getMessage() == null ? "Admin assistant service unavailable" : exception.getMessage())));
                } catch (IOException ignored) {
                    // The client disconnected; complete below.
                }
                emitter.complete();
            }
        });
        return emitter;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAssistantDraftActionResponseDTO getDraftAction(UUID adminUserId, UUID draftActionId) {
        return toDraftActionResponse(findDraftAction(adminUserId, draftActionId));
    }

    @Override
    @Transactional
    public AdminAssistantDraftActionResponseDTO executeDraftAction(UUID adminUserId, UUID draftActionId) {
        AdminAssistantDraftAction draftAction = findDraftAction(adminUserId, draftActionId);
        if (draftAction.getStatus() != AdminAssistantDraftActionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Draft action is not pending");
        }
        if (draftAction.getExpiresAt().isBefore(LocalDateTime.now())) {
            draftAction.setStatus(AdminAssistantDraftActionStatus.EXPIRED);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Draft action has expired");
        }
        try {
            Object result = executeAction(adminUserId, draftAction);
            draftAction.setStatus(AdminAssistantDraftActionStatus.EXECUTED);
            draftAction.setExecutedAt(LocalDateTime.now());
            draftAction.setExecutedByAdminUserId(adminUserId);
            draftAction.setExecutionResult(objectMapper.convertValue(result, MAP_TYPE));
            saveMessage(
                    draftAction.getConversation(),
                    AdminAssistantMessageRole.SYSTEM,
                    "Executed assistant draft action: " + draftAction.getActionType(),
                    Map.of("draftActionId", draftAction.getId(), "actionType", draftAction.getActionType()));
            return toDraftActionResponse(draftActionRepository.save(draftAction));
        } catch (RuntimeException exception) {
            draftAction.setStatus(AdminAssistantDraftActionStatus.FAILED);
            draftAction.setErrorMessage(exception.getMessage());
            draftActionRepository.save(draftAction);
            throw exception;
        }
    }

    private AdminAssistantWebhookResponseDTO callWebhook(
            AdminAssistantConversation conversation,
            AdminAssistantMessage userMessage,
            Map<String, Object> pageContext) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("conversationId", conversation.getId());
        request.put("messageId", userMessage.getId());
        request.put("adminUserId", conversation.getAdminUserId());
        request.put("message", userMessage.getContent());
        request.put("pageContext", pageContext == null ? Map.of() : pageContext);
        request.put("history", history(conversation.getId()));
        request.put("model", assistantModel);
        request.put("toolApi", Map.of(
                "baseUrl", toolBaseUrl,
                "tokenHeader", "X-Admin-Assistant-Tool-Token",
                "token", toolToken));

        AdminAssistantWebhookResponseDTO response;
        try {
            response = restClient.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AdminAssistantWebhookResponseDTO.class);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Admin assistant AI service unavailable");
        }
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Admin assistant AI service returned an empty response");
        }
        return response;
    }

    private List<Map<String, Object>> history(UUID conversationId) {
        List<AdminAssistantMessage> messages = new ArrayList<>(
                messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversationId));
        java.util.Collections.reverse(messages);
        return messages.stream()
                .map(message -> Map.<String, Object>of(
                        "role", message.getRole().name(),
                        "content", message.getContent(),
                        "createdAt", message.getCreatedAt()))
                .toList();
    }

    private AdminAssistantDraftAction createDraftActionIfValid(
            AdminAssistantConversation conversation,
            AdminAssistantMessage message,
            UUID adminUserId,
            AdminAssistantWebhookResponseDTO.DraftAction draft) {
        if (draft == null
                || draft.getActionType() == null
                || draft.getPayload() == null
                || draft.getPayload().isEmpty()
                || draft.getSourceRefs() == null
                || draft.getSourceRefs().isEmpty()) {
            return null;
        }
        AdminAssistantDraftAction draftAction = new AdminAssistantDraftAction();
        draftAction.setConversation(conversation);
        draftAction.setMessage(message);
        draftAction.setAdminUserId(adminUserId);
        draftAction.setActionType(draft.getActionType());
        draftAction.setPayload(new LinkedHashMap<>(draft.getPayload()));
        draftAction.setExplanation(blankToNull(draft.getExplanation()));
        draftAction.setSourceRefs(draft.getSourceRefs());
        draftAction.setExpiresAt(LocalDateTime.now().plusMinutes(DRAFT_EXPIRE_MINUTES));
        return draftActionRepository.save(draftAction);
    }

    private Object executeAction(UUID adminUserId, AdminAssistantDraftAction draftAction) {
        Map<String, Object> payload = draftAction.getPayload() == null ? Map.of() : draftAction.getPayload();
        return switch (draftAction.getActionType()) {
            case REPORT_RESOLVE -> contentReportService.resolveReport(requiredUuid(payload, "reportId"));
            case REPORT_REJECT -> {
                AdminContentReportStatusUpdateRequestDTO request = new AdminContentReportStatusUpdateRequestDTO();
                request.setStatus(ReportStatus.REJECTED);
                yield contentReportService.updateStatus(requiredUuid(payload, "reportId"), request);
            }
            case REPORT_ASK_AI_RESOLUTION -> reportAiResolutionService.createResolution(
                    requiredUuid(payload, "reportId"),
                    new AdminReportAiResolutionCreateRequestDTO(),
                    adminUserId);
            case REPORT_CANCEL_AUTO_APPLY -> autoApplyJobService.cancelJob(requiredUuid(payload, "jobId"), adminUserId);
            case BLOG_HIDE -> updateBlog(requiredUuid(payload, "blogId"), PostStatus.HIDDEN);
            case BLOG_REMOVE -> updateBlog(requiredUuid(payload, "blogId"), PostStatus.REMOVED);
            case COMMENT_HIDE -> updateComment(requiredUuid(payload, "commentId"), PostStatus.HIDDEN);
            case COMMENT_REMOVE -> updateComment(requiredUuid(payload, "commentId"), PostStatus.REMOVED);
            case USER_DEACTIVATE -> {
                AdminUserStatusUpdateRequestDTO request = new AdminUserStatusUpdateRequestDTO();
                request.setAccountStatus(false);
                yield adminUserService.updateUserStatus(requiredUuid(payload, "userId"), request);
            }
            case CAFE_PAGE_SUSPEND -> {
                AdminCafePageStatusUpdateRequestDTO request = new AdminCafePageStatusUpdateRequestDTO();
                request.setStatus(PageStatus.SUSPENDED);
                yield adminCafePageService.updateCafePageStatus(requiredUuid(payload, "pageId"), request);
            }
        };
    }

    private Object updateBlog(UUID blogId, PostStatus status) {
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(status);
        return adminBlogService.updateBlogStatus(blogId, request);
    }

    private Object updateComment(UUID commentId, PostStatus status) {
        AdminPostStatusUpdateRequestDTO request = new AdminPostStatusUpdateRequestDTO();
        request.setStatus(status);
        return adminCommentService.updateCommentStatus(commentId, request);
    }

    private AdminAssistantMessage saveMessage(
            AdminAssistantConversation conversation,
            AdminAssistantMessageRole role,
            String content,
            Map<String, Object> metadata) {
        AdminAssistantMessage message = new AdminAssistantMessage();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        message.setMetadata(metadata == null ? Map.of() : metadata);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return messageRepository.save(message);
    }

    private AdminAssistantConversation findConversation(UUID adminUserId, UUID conversationId) {
        if (conversationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conversationId is required");
        }
        return conversationRepository.findByIdAndAdminUserId(conversationId, requireAdminUserId(adminUserId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private AdminAssistantDraftAction findDraftAction(UUID adminUserId, UUID draftActionId) {
        if (draftActionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "draftActionId is required");
        }
        return draftActionRepository.findByIdAndAdminUserId(draftActionId, requireAdminUserId(adminUserId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft action not found"));
    }

    private UUID requireAdminUserId(UUID adminUserId) {
        if (adminUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin user is required");
        }
        return adminUserId;
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "New assistant chat";
        }
        String normalized = title.trim();
        return normalized.length() > 160 ? normalized.substring(0, 160) : normalized;
    }

    private String normalizeMessage(AdminAssistantMessageRequestDTO request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required");
        }
        return request.getMessage().trim();
    }

    private UUID requiredUuid(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is required");
        }
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " must be a valid UUID");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<Map<String, Object>> safeList(List<Map<String, Object>> value) {
        return value == null ? List.of() : value;
    }

    private AdminAssistantConversationResponseDTO toConversationResponse(AdminAssistantConversation conversation) {
        AdminAssistantConversationResponseDTO response = new AdminAssistantConversationResponseDTO();
        response.setId(conversation.getId());
        response.setAdminUserId(conversation.getAdminUserId());
        response.setTitle(conversation.getTitle());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        return response;
    }

    private AdminAssistantMessageResponseDTO toMessageResponse(AdminAssistantMessage message) {
        AdminAssistantMessageResponseDTO response = new AdminAssistantMessageResponseDTO();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setMetadata(message.getMetadata());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private AdminAssistantDraftActionResponseDTO toDraftActionResponse(AdminAssistantDraftAction draftAction) {
        AdminAssistantDraftActionResponseDTO response = new AdminAssistantDraftActionResponseDTO();
        response.setId(draftAction.getId());
        response.setConversationId(draftAction.getConversation().getId());
        response.setMessageId(draftAction.getMessage() == null ? null : draftAction.getMessage().getId());
        response.setActionType(draftAction.getActionType());
        response.setPayload(draftAction.getPayload());
        response.setExplanation(draftAction.getExplanation());
        response.setSourceRefs(draftAction.getSourceRefs());
        response.setStatus(draftAction.getStatus());
        response.setExpiresAt(draftAction.getExpiresAt());
        response.setExecutedAt(draftAction.getExecutedAt());
        response.setExecutionResult(draftAction.getExecutionResult());
        response.setErrorMessage(draftAction.getErrorMessage());
        response.setCreatedAt(draftAction.getCreatedAt());
        response.setUpdatedAt(draftAction.getUpdatedAt());
        return response;
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        return requestFactory;
    }
}
