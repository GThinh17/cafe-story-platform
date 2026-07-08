package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;
import com.cafestory.entity.AdminAssistantConversation;
import com.cafestory.entity.AdminAssistantMessage;
import com.cafestory.entity.AdminAssistantToolCall;
import com.cafestory.entity.enums.AdminAssistantToolCallStatus;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.AdminAssistantConversationRepository;
import com.cafestory.repository.AdminAssistantMessageRepository;
import com.cafestory.repository.AdminAssistantToolCallRepository;
import com.cafestory.service.serviceInterface.AdminAssistantToolService;
import com.cafestory.service.serviceInterface.AdminBlogService;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import com.cafestory.service.serviceInterface.AdminCommentService;
import com.cafestory.service.serviceInterface.AdminDashboardService;
import com.cafestory.service.serviceInterface.AdminModerationService;
import com.cafestory.service.serviceInterface.AdminUserService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminAssistantToolServiceImpl implements AdminAssistantToolService {

    private final AdminDashboardService adminDashboardService;
    private final ContentReportService contentReportService;
    private final AdminModerationService adminModerationService;
    private final AdminUserService adminUserService;
    private final AdminBlogService adminBlogService;
    private final AdminCommentService adminCommentService;
    private final AdminCafePageService adminCafePageService;
    private final AdminAssistantConversationRepository conversationRepository;
    private final AdminAssistantMessageRepository messageRepository;
    private final AdminAssistantToolCallRepository toolCallRepository;
    private final ObjectMapper objectMapper;

    public AdminAssistantToolServiceImpl(
            AdminDashboardService adminDashboardService,
            ContentReportService contentReportService,
            AdminModerationService adminModerationService,
            AdminUserService adminUserService,
            AdminBlogService adminBlogService,
            AdminCommentService adminCommentService,
            AdminCafePageService adminCafePageService,
            AdminAssistantConversationRepository conversationRepository,
            AdminAssistantMessageRepository messageRepository,
            AdminAssistantToolCallRepository toolCallRepository,
            ObjectMapper objectMapper) {
        this.adminDashboardService = adminDashboardService;
        this.contentReportService = contentReportService;
        this.adminModerationService = adminModerationService;
        this.adminUserService = adminUserService;
        this.adminBlogService = adminBlogService;
        this.adminCommentService = adminCommentService;
        this.adminCafePageService = adminCafePageService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.toolCallRepository = toolCallRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AdminAssistantToolResponseDTO executeTool(
            String toolName,
            AdminAssistantToolRequestDTO request,
            UUID fallbackAdminUserId) {
        String normalizedToolName = normalizeToolName(toolName);
        Map<String, Object> input = request == null || request.getInput() == null ? Map.of() : request.getInput();
        UUID adminUserId = request == null || request.getAdminUserId() == null
                ? fallbackAdminUserId
                : request.getAdminUserId();
        AdminAssistantConversation conversation = findConversation(request == null ? null : request.getConversationId(), adminUserId);
        AdminAssistantMessage message = findMessage(request == null ? null : request.getMessageId());
        long started = System.currentTimeMillis();

        try {
            Object data = dispatch(normalizedToolName, input);
            AdminAssistantToolResponseDTO response = toToolResponse(normalizedToolName, data);
            saveToolCall(conversation, message, normalizedToolName, input, response, null, started);
            return response;
        } catch (RuntimeException exception) {
            saveToolCall(conversation, message, normalizedToolName, input, null, exception, started);
            throw exception;
        }
    }

    private Object dispatch(String toolName, Map<String, Object> input) {
        return switch (toolName) {
            case "dashboard_summary" -> adminDashboardService.getSummary();
            case "search_reports" -> contentReportService.getReports(
                    enumValue(input, "status", ReportStatus.class),
                    enumValue(input, "targetType", ReportTargetType.class),
                    pageable(input));
            case "get_report_detail" -> contentReportService.getReport(requiredUuid(input, "reportId"));
            case "get_moderation_queue_summary" -> Map.of(
                    "queue", adminModerationService.getQueue(pageable(input)),
                    "jobs", adminModerationService.getJobs(
                            enumValue(input, "status", ReportModerationJobStatus.class),
                            pageable(input)));
            case "get_user_summary" -> adminUserService.getUser(requiredUuid(input, "userId"));
            case "search_users" -> adminUserService.getUsers(
                    stringValue(input, "search"),
                    booleanValue(input, "accountStatus"),
                    enumValue(input, "role", UserRole.class),
                    pageable(input));
            case "get_blog_summary" -> adminBlogService.getBlog(requiredUuid(input, "blogId"));
            case "search_blogs" -> adminBlogService.getBlogs(
                    enumValue(input, "status", PostStatus.class),
                    uuidValue(input, "authorUserId"),
                    uuidValue(input, "pageId"),
                    pageable(input));
            case "get_comment_summary" -> adminCommentService.getComment(requiredUuid(input, "commentId"));
            case "search_comments" -> adminCommentService.getComments(
                    enumValue(input, "status", PostStatus.class),
                    uuidValue(input, "blogId"),
                    uuidValue(input, "userId"),
                    pageable(input));
            case "get_cafe_page_summary" -> adminCafePageService.getCafePage(requiredUuid(input, "pageId"));
            case "search_cafe_pages" -> adminCafePageService.getCafePages(
                    enumValue(input, "status", PageStatus.class),
                    uuidValue(input, "ownerUserId"),
                    pageable(input));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported assistant tool: " + toolName);
        };
    }

    private AdminAssistantConversation findConversation(UUID conversationId, UUID adminUserId) {
        if (conversationId == null || adminUserId == null) {
            return null;
        }
        return conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId).orElse(null);
    }

    private AdminAssistantMessage findMessage(UUID messageId) {
        if (messageId == null) {
            return null;
        }
        return messageRepository.findById(messageId).orElse(null);
    }

    private void saveToolCall(
            AdminAssistantConversation conversation,
            AdminAssistantMessage message,
            String toolName,
            Map<String, Object> input,
            AdminAssistantToolResponseDTO response,
            RuntimeException exception,
            long started) {
        if (conversation == null) {
            return;
        }
        AdminAssistantToolCall toolCall = new AdminAssistantToolCall();
        toolCall.setConversation(conversation);
        toolCall.setMessage(message);
        toolCall.setToolName(toolName);
        toolCall.setInputPayload(new LinkedHashMap<>(input));
        toolCall.setStatus(exception == null ? AdminAssistantToolCallStatus.SUCCESS : AdminAssistantToolCallStatus.FAILED);
        toolCall.setDurationMs(System.currentTimeMillis() - started);
        if (exception == null) {
            toolCall.setOutputSummary(maskedSummary(response));
        } else {
            toolCall.setErrorMessage(exception.getMessage());
        }
        toolCallRepository.save(toolCall);
    }

    private AdminAssistantToolResponseDTO toToolResponse(String toolName, Object data) {
        List<String> maskedFields = new ArrayList<>();
        AdminAssistantToolResponseDTO response = new AdminAssistantToolResponseDTO();
        response.setToolName(toolName);
        response.setData(maskValue(data, "$", maskedFields));
        response.setMaskedFields(maskedFields);
        response.setSourceRefs(List.of(Map.of("type", "tool", "toolName", toolName)));
        response.setGeneratedAt(LocalDateTime.now());
        return response;
    }

    @SuppressWarnings("unchecked")
    private Object maskValue(Object value, String path, List<String> maskedFields) {
        Object converted = objectMapper.convertValue(value, Object.class);
        if (converted instanceof Map<?, ?> sourceMap) {
            Map<String, Object> target = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object rawValue = entry.getValue();
                String childPath = path + "." + key;
                if (isSensitiveKey(key) && rawValue != null) {
                    target.put(key, maskScalar(key, String.valueOf(rawValue)));
                    maskedFields.add(childPath);
                } else {
                    target.put(key, maskValue(rawValue, childPath, maskedFields));
                }
            }
            return target;
        }
        if (converted instanceof List<?> sourceList) {
            List<Object> target = new ArrayList<>();
            for (int index = 0; index < sourceList.size(); index++) {
                target.add(maskValue(sourceList.get(index), path + "[" + index + "]", maskedFields));
            }
            return target;
        }
        return converted;
    }

    private Map<String, Object> maskedSummary(AdminAssistantToolResponseDTO response) {
        if (response == null) {
            return Map.of();
        }
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("toolName", response.getToolName());
        value.put("maskedFields", response.getMaskedFields());
        value.put("sourceRefs", response.getSourceRefs());
        value.put("generatedAt", response.getGeneratedAt() == null ? null : response.getGeneratedAt().toString());
        Object data = response.getData();
        if (data instanceof Map<?, ?> map && map.containsKey("content")) {
            value.put("contentSize", ((List<?>) map.get("content")).size());
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("email")
                || normalized.contains("phone");
    }

    private String maskScalar(String key, String value) {
        String normalized = key.toLowerCase();
        if (normalized.contains("email")) {
            int at = value.indexOf('@');
            if (at <= 1) {
                return "***";
            }
            return value.charAt(0) + "***" + value.substring(at);
        }
        if (normalized.contains("phone")) {
            return value.length() <= 4 ? "****" : value.substring(0, 2) + "******" + value.substring(value.length() - 2);
        }
        return "***";
    }

    private Pageable pageable(Map<String, Object> input) {
        int page = Math.max(0, intValue(input, "page", 0));
        int size = Math.min(Math.max(1, intValue(input, "size", 10)), 50);
        return PageRequest.of(page, size);
    }

    private String normalizeToolName(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toolName is required");
        }
        return toolName.trim();
    }

    private UUID requiredUuid(Map<String, Object> input, String key) {
        UUID value = uuidValue(input, key);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is required");
        }
        return value;
    }

    private UUID uuidValue(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " must be a valid UUID");
        }
    }

    private String stringValue(Map<String, Object> input, String key) {
        Object value = input.get(key);
        return value == null || String.valueOf(value).isBlank() ? null : String.valueOf(value).trim();
    }

    private Boolean booleanValue(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int intValue(Map<String, Object> input, String key, int defaultValue) {
        Object value = input.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private <T extends Enum<T>> T enumValue(Map<String, Object> input, String key, Class<T> enumType) {
        Object value = input.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, String.valueOf(value).trim());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is invalid");
        }
    }
}
