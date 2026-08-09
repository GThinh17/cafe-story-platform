package com.cafestory.service;

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
import com.cafestory.service.serviceImplement.AdminAssistantServiceImpl;
import com.cafestory.service.serviceInterface.AdminBlogService;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import com.cafestory.service.serviceInterface.AdminCommentService;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.cafestory.service.serviceInterface.AdminUserService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminAssistantServiceImpl}.
 *
 * <p>Lớp này dựng {@code RestClient} ngay trong hàm khởi tạo nên không mock được
 * bằng Mockito. Thay vào đó bộ kiểm thử dựng một {@link HttpServer} của JDK trên
 * cổng loopback ngẫu nhiên và trỏ webhook vào đó: n8n không hề được gọi, mọi
 * phản hồi đều do chính bài kiểm thử quyết định.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminAssistantServiceImplTest {

    @Mock
    private AdminAssistantConversationRepository conversationRepository;
    @Mock
    private AdminAssistantMessageRepository messageRepository;
    @Mock
    private AdminAssistantDraftActionRepository draftActionRepository;
    @Mock
    private ContentReportService contentReportService;
    @Mock
    private AdminReportAiResolutionService reportAiResolutionService;
    @Mock
    private AdminReportAiAutoApplyJobService autoApplyJobService;
    @Mock
    private AdminBlogService adminBlogService;
    @Mock
    private AdminCommentService adminCommentService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AdminCafePageService adminCafePageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServer webhookServer;
    private final AtomicReference<String> webhookBody = new AtomicReference<>("{}");
    private final AtomicInteger webhookStatus = new AtomicInteger(200);

    private AdminAssistantServiceImpl assistantService;
    private UUID adminUserId;
    private AdminAssistantConversation conversation;

    @BeforeEach
    void setUp() throws IOException {
        webhookServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        webhookServer.createContext("/webhook", exchange -> {
            byte[] payload = webhookBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(webhookStatus.get(), payload.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(payload);
            }
        });
        webhookServer.start();

        assistantService = new AdminAssistantServiceImpl(
                conversationRepository,
                messageRepository,
                draftActionRepository,
                contentReportService,
                reportAiResolutionService,
                autoApplyJobService,
                adminBlogService,
                adminCommentService,
                adminUserService,
                adminCafePageService,
                objectMapper,
                "http://127.0.0.1:" + webhookServer.getAddress().getPort() + "/webhook",
                5_000,
                "gpt-4o-mini",
                "http://localhost:8080/api/admin/assistant/tools",
                new AdminAssistantSecurityProperties("tool-token-secret"));

        adminUserId = UUID.randomUUID();
        conversation = conversation(adminUserId);

        when(conversationRepository.save(any(AdminAssistantConversation.class)))
                .thenAnswer(invocation -> {
                    AdminAssistantConversation saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(UUID.randomUUID());
                    }
                    return saved;
                });
        when(messageRepository.save(any(AdminAssistantMessage.class)))
                .thenAnswer(invocation -> {
                    AdminAssistantMessage saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    saved.setCreatedAt(LocalDateTime.now());
                    return saved;
                });
        when(messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(any(UUID.class)))
                .thenReturn(List.of(message(AdminAssistantMessageRole.USER, "cau hoi truoc")));
        when(draftActionRepository.save(any(AdminAssistantDraftAction.class)))
                .thenAnswer(invocation -> {
                    AdminAssistantDraftAction saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(UUID.randomUUID());
                    }
                    return saved;
                });
    }

    @AfterEach
    void tearDown() {
        webhookServer.stop(0);
    }

    // ---------------------------------------------------------- Hội thoại

    @Test
    void createConversation_success_defaultTitleWhenRequestIsNull_TC001() {
        AdminAssistantConversationResponseDTO result = assistantService.createConversation(adminUserId, null);

        assertThat(result.getTitle()).isEqualTo("New assistant chat");
        assertThat(result.getAdminUserId()).isEqualTo(adminUserId);
    }

    @Test
    void createConversation_success_trimsAndTruncatesTitle_TC002() {
        AdminAssistantConversationCreateRequestDTO request = new AdminAssistantConversationCreateRequestDTO();
        request.setTitle("  " + "x".repeat(200) + "  ");

        AdminAssistantConversationResponseDTO result = assistantService.createConversation(adminUserId, request);

        assertThat(result.getTitle()).hasSize(160);
    }

    @Test
    void createConversation_success_blankTitleFallsBackToDefault_TC003() {
        AdminAssistantConversationCreateRequestDTO request = new AdminAssistantConversationCreateRequestDTO();
        request.setTitle("   ");

        assertThat(assistantService.createConversation(adminUserId, request).getTitle())
                .isEqualTo("New assistant chat");
    }

    @Test
    void createConversation_fail_missingAdminUser_TC004() {
        assertThatThrownBy(() -> assistantService.createConversation(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin user is required");
    }

    @Test
    void getConversations_success_mapsPage_TC005() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(conversationRepository.findByAdminUserId(adminUserId, pageable))
                .thenReturn(new PageImpl<>(List.of(conversation)));

        Page<AdminAssistantConversationResponseDTO> result =
                assistantService.getConversations(adminUserId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(conversation.getId());
    }

    @Test
    void getMessages_success_mapsPage_TC006() {
        PageRequest pageable = PageRequest.of(0, 10);
        AdminAssistantMessage message = message(AdminAssistantMessageRole.ASSISTANT, "tra loi");
        when(conversationRepository.findByIdAndAdminUserId(conversation.getId(), adminUserId))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationId(conversation.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(message)));

        Page<AdminAssistantMessageResponseDTO> result =
                assistantService.getMessages(adminUserId, conversation.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(AdminAssistantMessageRole.ASSISTANT);
        assertThat(result.getContent().get(0).getConversationId()).isEqualTo(conversation.getId());
    }

    @Test
    void getMessages_fail_conversationIdMissing_TC007() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> assistantService.getMessages(adminUserId, null, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("conversationId is required");
    }

    @Test
    void getMessages_fail_conversationNotOwnedByAdmin_TC008() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID conversationId = conversation.getId();
        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> assistantService.getMessages(adminUserId, conversationId, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Conversation not found");
    }

    // -------------------------------------------------------- sendMessage

    @Test
    void sendMessage_success_persistsAnswerAndDraftAction_TC009() {
        UUID reportId = UUID.randomUUID();
        givenConversationFound();
        webhookBody.set("""
                {
                  "answer": "  Nen an bai viet nay.  ",
                  "modelName": "gpt-4o",
                  "citations": [{"source": "report"}],
                  "toolCalls": [{"name": "listReports"}],
                  "draftAction": {
                    "actionType": "BLOG_HIDE",
                    "payload": {"blogId": "%s"},
                    "explanation": "  vi pham noi quy  ",
                    "sourceRefs": [{"reportId": "%s"}]
                  }
                }
                """.formatted(reportId, reportId));

        AdminAssistantChatResponseDTO result =
                assistantService.sendMessage(adminUserId, conversation.getId(), messageRequest("  can xu ly  "));

        assertThat(result.getMessage().getContent()).isEqualTo("Nen an bai viet nay.");
        assertThat(result.getMessage().getMetadata()).containsEntry("modelName", "gpt-4o");
        assertThat(result.getCitations()).hasSize(1);
        assertThat(result.getToolCalls()).hasSize(1);
        assertThat(result.getDraftAction()).isNotNull();
        assertThat(result.getDraftAction().getActionType()).isEqualTo(AdminAssistantDraftActionType.BLOG_HIDE);
        assertThat(result.getDraftAction().getExplanation()).isEqualTo("vi pham noi quy");
        assertThat(result.getDraftAction().getExpiresAt()).isAfter(LocalDateTime.now());

        ArgumentCaptor<AdminAssistantMessage> captor = ArgumentCaptor.forClass(AdminAssistantMessage.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getRole()).isEqualTo(AdminAssistantMessageRole.USER);
        assertThat(captor.getAllValues().get(0).getContent()).isEqualTo("can xu ly");
    }

    @Test
    void sendMessage_success_fallsBackWhenAnswerBlank_TC010() {
        givenConversationFound();
        webhookBody.set("{\"answer\":\"   \"}");

        AdminAssistantChatResponseDTO result =
                assistantService.sendMessage(adminUserId, conversation.getId(), messageRequest("hoi"));

        assertThat(result.getMessage().getContent()).isEqualTo("I could not produce an admin assistant response.");
        assertThat(result.getMessage().getMetadata()).containsEntry("modelName", "gpt-4o-mini");
        assertThat(result.getCitations()).isEmpty();
        assertThat(result.getToolCalls()).isEmpty();
        assertThat(result.getDraftAction()).isNull();
    }

    @Test
    void sendMessage_success_ignoresIncompleteDraftAction_TC011() {
        givenConversationFound();
        webhookBody.set("""
                {"answer":"ok","draftAction":{"actionType":"BLOG_HIDE","payload":{},"sourceRefs":[]}}
                """);

        AdminAssistantChatResponseDTO result =
                assistantService.sendMessage(adminUserId, conversation.getId(), messageRequest("hoi"));

        assertThat(result.getDraftAction()).isNull();
        verify(draftActionRepository, never()).save(any(AdminAssistantDraftAction.class));
    }

    @Test
    void sendMessage_success_ignoresDraftActionWithoutSourceRefs_TC012() {
        givenConversationFound();
        webhookBody.set("""
                {"answer":"ok","draftAction":{"actionType":"BLOG_HIDE","payload":{"blogId":"x"}}}
                """);

        assertThat(assistantService.sendMessage(adminUserId, conversation.getId(), messageRequest("hoi"))
                .getDraftAction()).isNull();
    }

    @Test
    void sendMessage_success_keepsPageContextInUserMetadata_TC013() {
        givenConversationFound();
        webhookBody.set("{\"answer\":\"ok\"}");
        AdminAssistantMessageRequestDTO request = messageRequest("hoi");
        request.setPageContext(Map.of("route", "/admin/reports"));

        assistantService.sendMessage(adminUserId, conversation.getId(), request);

        ArgumentCaptor<AdminAssistantMessage> captor = ArgumentCaptor.forClass(AdminAssistantMessage.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getMetadata())
                .containsEntry("pageContext", Map.of("route", "/admin/reports"));
    }

    @Test
    void sendMessage_fail_blankMessage_TC014() {
        givenConversationFound();
        UUID conversationId = conversation.getId();

        assertThatThrownBy(() ->
                assistantService.sendMessage(adminUserId, conversationId, messageRequest("   ")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("message is required");

        assertThatThrownBy(() -> assistantService.sendMessage(adminUserId, conversationId, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("message is required");
    }

    @Test
    void sendMessage_fail_webhookReturnsServerError_TC015() {
        givenConversationFound();
        webhookStatus.set(500);
        webhookBody.set("{}");
        UUID conversationId = conversation.getId();

        assertThatThrownBy(() ->
                assistantService.sendMessage(adminUserId, conversationId, messageRequest("hoi")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin assistant AI service unavailable")
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void sendMessage_fail_webhookReturnsEmptyBody_TC016() {
        givenConversationFound();
        webhookBody.set("");
        UUID conversationId = conversation.getId();

        assertThatThrownBy(() ->
                assistantService.sendMessage(adminUserId, conversationId, messageRequest("hoi")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("empty response");
    }

    // ------------------------------------------------------- streamMessage

    @Test
    void streamMessage_success_emitsMessageThenCompletes_TC017() {
        givenConversationFound();
        webhookBody.set("{\"answer\":\"ok\"}");

        SseEmitter emitter = assistantService.streamMessage(
                adminUserId, conversation.getId(), messageRequest("hoi"));

        assertThat(emitter).isNotNull();
        // streamMessage chạy nền trên common pool; chờ pool rảnh để phép kiểm
        // không phụ thuộc thời gian ngủ cố định.
        assertThat(ForkJoinPool.commonPool().awaitQuiescence(15, TimeUnit.SECONDS)).isTrue();
        verify(messageRepository, org.mockito.Mockito.times(2)).save(any(AdminAssistantMessage.class));
    }

    @Test
    void streamMessage_success_completesEvenWhenSendMessageFails_TC018() {
        UUID conversationId = conversation.getId();
        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.empty());

        SseEmitter emitter = assistantService.streamMessage(adminUserId, conversationId, messageRequest("hoi"));

        assertThat(emitter).isNotNull();
        assertThat(ForkJoinPool.commonPool().awaitQuiescence(15, TimeUnit.SECONDS)).isTrue();
        verify(messageRepository, never()).save(any(AdminAssistantMessage.class));
    }

    // ---------------------------------------------------- Draft action CRUD

    @Test
    void getDraftAction_success_mapsEveryField_TC019() {
        AdminAssistantDraftAction draftAction = draftAction(AdminAssistantDraftActionType.BLOG_HIDE, Map.of("blogId", UUID.randomUUID().toString()));
        when(draftActionRepository.findByIdAndAdminUserId(draftAction.getId(), adminUserId))
                .thenReturn(Optional.of(draftAction));

        AdminAssistantDraftActionResponseDTO result =
                assistantService.getDraftAction(adminUserId, draftAction.getId());

        assertThat(result.getId()).isEqualTo(draftAction.getId());
        assertThat(result.getConversationId()).isEqualTo(conversation.getId());
        assertThat(result.getMessageId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AdminAssistantDraftActionStatus.PENDING);
    }

    @Test
    void getDraftAction_success_nullMessageIsTolerated_TC020() {
        AdminAssistantDraftAction draftAction = draftAction(AdminAssistantDraftActionType.BLOG_HIDE, Map.of());
        draftAction.setMessage(null);
        when(draftActionRepository.findByIdAndAdminUserId(draftAction.getId(), adminUserId))
                .thenReturn(Optional.of(draftAction));

        assertThat(assistantService.getDraftAction(adminUserId, draftAction.getId()).getMessageId()).isNull();
    }

    @Test
    void getDraftAction_fail_missingId_TC021() {
        assertThatThrownBy(() -> assistantService.getDraftAction(adminUserId, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("draftActionId is required");
    }

    @Test
    void getDraftAction_fail_notFound_TC022() {
        UUID draftActionId = UUID.randomUUID();
        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> assistantService.getDraftAction(adminUserId, draftActionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Draft action not found");
    }

    // ------------------------------------------------- executeDraftAction

    @ParameterizedTest
    @EnumSource(AdminAssistantDraftActionType.class)
    void executeDraftAction_success_runsEveryActionType_TC023(AdminAssistantDraftActionType actionType) {
        UUID targetId = UUID.randomUUID();
        AdminAssistantDraftAction draftAction = draftAction(actionType, payloadFor(actionType, targetId));
        when(draftActionRepository.findByIdAndAdminUserId(draftAction.getId(), adminUserId))
                .thenReturn(Optional.of(draftAction));
        stubActionTarget(actionType);

        AdminAssistantDraftActionResponseDTO result =
                assistantService.executeDraftAction(adminUserId, draftAction.getId());

        assertThat(result.getStatus()).isEqualTo(AdminAssistantDraftActionStatus.EXECUTED);
        assertThat(draftAction.getExecutedByAdminUserId()).isEqualTo(adminUserId);
        assertThat(draftAction.getExecutedAt()).isNotNull();
        verifyActionTarget(actionType, targetId);
        verify(messageRepository).save(any(AdminAssistantMessage.class));
    }

    @Test
    void executeDraftAction_fail_notPending_TC024() {
        AdminAssistantDraftAction draftAction = draftAction(AdminAssistantDraftActionType.BLOG_HIDE, Map.of());
        draftAction.setStatus(AdminAssistantDraftActionStatus.EXECUTED);
        UUID draftActionId = draftAction.getId();
        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.of(draftAction));

        assertThatThrownBy(() -> assistantService.executeDraftAction(adminUserId, draftActionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Draft action is not pending");
    }

    @Test
    void executeDraftAction_fail_expired_TC025() {
        AdminAssistantDraftAction draftAction = draftAction(AdminAssistantDraftActionType.BLOG_HIDE, Map.of());
        draftAction.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        UUID draftActionId = draftAction.getId();
        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.of(draftAction));

        assertThatThrownBy(() -> assistantService.executeDraftAction(adminUserId, draftActionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Draft action has expired");
        assertThat(draftAction.getStatus()).isEqualTo(AdminAssistantDraftActionStatus.EXPIRED);
    }

    @Test
    void executeDraftAction_fail_missingPayloadKeyMarksFailed_TC026() {
        AdminAssistantDraftAction draftAction = draftAction(AdminAssistantDraftActionType.BLOG_HIDE, Map.of());
        UUID draftActionId = draftAction.getId();
        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.of(draftAction));

        assertThatThrownBy(() -> assistantService.executeDraftAction(adminUserId, draftActionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("blogId is required");

        assertThat(draftAction.getStatus()).isEqualTo(AdminAssistantDraftActionStatus.FAILED);
        assertThat(draftAction.getErrorMessage()).contains("blogId is required");
        verify(draftActionRepository).save(draftAction);
    }

    @Test
    void executeDraftAction_fail_payloadValueIsNotUuid_TC027() {
        AdminAssistantDraftAction draftAction =
                draftAction(AdminAssistantDraftActionType.BLOG_HIDE, Map.of("blogId", "khong-phai-uuid"));
        UUID draftActionId = draftAction.getId();
        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.of(draftAction));

        assertThatThrownBy(() -> assistantService.executeDraftAction(adminUserId, draftActionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("blogId must be a valid UUID");
    }

    @Test
    void executeDraftAction_fail_nullPayloadTreatedAsEmpty_TC028() {
        AdminAssistantDraftAction draftAction = draftAction(AdminAssistantDraftActionType.REPORT_RESOLVE, null);
        UUID draftActionId = draftAction.getId();
        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.of(draftAction));

        assertThatThrownBy(() -> assistantService.executeDraftAction(adminUserId, draftActionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("reportId is required");
    }

    // ------------------------------------------------------------- Helpers

    private void givenConversationFound() {
        when(conversationRepository.findByIdAndAdminUserId(conversation.getId(), adminUserId))
                .thenReturn(Optional.of(conversation));
    }

    private Map<String, Object> payloadFor(AdminAssistantDraftActionType actionType, UUID targetId) {
        String key = switch (actionType) {
            case REPORT_RESOLVE, REPORT_REJECT, REPORT_ASK_AI_RESOLUTION -> "reportId";
            case REPORT_CANCEL_AUTO_APPLY -> "jobId";
            case BLOG_HIDE, BLOG_REMOVE -> "blogId";
            case COMMENT_HIDE, COMMENT_REMOVE -> "commentId";
            case USER_DEACTIVATE -> "userId";
            case CAFE_PAGE_SUSPEND -> "pageId";
        };
        return Map.of(key, targetId.toString());
    }

    private void stubActionTarget(AdminAssistantDraftActionType actionType) {
        Map<String, Object> result = Map.of("ok", true);
        switch (actionType) {
            case REPORT_RESOLVE -> when(contentReportService.resolveReport(any(UUID.class))).thenReturn(null);
            case REPORT_REJECT -> when(contentReportService.updateStatus(
                    any(UUID.class), any(AdminContentReportStatusUpdateRequestDTO.class))).thenReturn(null);
            case REPORT_ASK_AI_RESOLUTION -> when(reportAiResolutionService.createResolution(
                    any(UUID.class), any(AdminReportAiResolutionCreateRequestDTO.class), any(UUID.class)))
                    .thenReturn(null);
            case REPORT_CANCEL_AUTO_APPLY -> when(autoApplyJobService.cancelJob(any(UUID.class), any(UUID.class)))
                    .thenReturn(null);
            case BLOG_HIDE, BLOG_REMOVE -> when(adminBlogService.updateBlogStatus(
                    any(UUID.class), any(AdminPostStatusUpdateRequestDTO.class))).thenReturn(null);
            case COMMENT_HIDE, COMMENT_REMOVE -> when(adminCommentService.updateCommentStatus(
                    any(UUID.class), any(AdminPostStatusUpdateRequestDTO.class))).thenReturn(null);
            case USER_DEACTIVATE -> when(adminUserService.updateUserStatus(
                    any(UUID.class), any(AdminUserStatusUpdateRequestDTO.class))).thenReturn(null);
            case CAFE_PAGE_SUSPEND -> when(adminCafePageService.updateCafePageStatus(
                    any(UUID.class), any(AdminCafePageStatusUpdateRequestDTO.class))).thenReturn(null);
        }
        assertThat(result).isNotEmpty();
    }

    private void verifyActionTarget(AdminAssistantDraftActionType actionType, UUID targetId) {
        switch (actionType) {
            case REPORT_RESOLVE -> verify(contentReportService).resolveReport(targetId);
            case REPORT_REJECT -> {
                ArgumentCaptor<AdminContentReportStatusUpdateRequestDTO> captor =
                        ArgumentCaptor.forClass(AdminContentReportStatusUpdateRequestDTO.class);
                verify(contentReportService).updateStatus(org.mockito.ArgumentMatchers.eq(targetId), captor.capture());
                assertThat(captor.getValue().getStatus()).isEqualTo(ReportStatus.REJECTED);
            }
            case REPORT_ASK_AI_RESOLUTION -> verify(reportAiResolutionService).createResolution(
                    org.mockito.ArgumentMatchers.eq(targetId),
                    any(AdminReportAiResolutionCreateRequestDTO.class),
                    org.mockito.ArgumentMatchers.eq(adminUserId));
            case REPORT_CANCEL_AUTO_APPLY -> verify(autoApplyJobService).cancelJob(targetId, adminUserId);
            case BLOG_HIDE, BLOG_REMOVE -> {
                ArgumentCaptor<AdminPostStatusUpdateRequestDTO> captor =
                        ArgumentCaptor.forClass(AdminPostStatusUpdateRequestDTO.class);
                verify(adminBlogService).updateBlogStatus(org.mockito.ArgumentMatchers.eq(targetId), captor.capture());
                assertThat(captor.getValue().getStatus()).isEqualTo(
                        actionType == AdminAssistantDraftActionType.BLOG_HIDE ? PostStatus.HIDDEN : PostStatus.REMOVED);
            }
            case COMMENT_HIDE, COMMENT_REMOVE -> {
                ArgumentCaptor<AdminPostStatusUpdateRequestDTO> captor =
                        ArgumentCaptor.forClass(AdminPostStatusUpdateRequestDTO.class);
                verify(adminCommentService).updateCommentStatus(
                        org.mockito.ArgumentMatchers.eq(targetId), captor.capture());
                assertThat(captor.getValue().getStatus()).isEqualTo(
                        actionType == AdminAssistantDraftActionType.COMMENT_HIDE
                                ? PostStatus.HIDDEN : PostStatus.REMOVED);
            }
            case USER_DEACTIVATE -> {
                ArgumentCaptor<AdminUserStatusUpdateRequestDTO> captor =
                        ArgumentCaptor.forClass(AdminUserStatusUpdateRequestDTO.class);
                verify(adminUserService).updateUserStatus(org.mockito.ArgumentMatchers.eq(targetId), captor.capture());
                assertThat(captor.getValue().getAccountStatus()).isFalse();
            }
            case CAFE_PAGE_SUSPEND -> {
                ArgumentCaptor<AdminCafePageStatusUpdateRequestDTO> captor =
                        ArgumentCaptor.forClass(AdminCafePageStatusUpdateRequestDTO.class);
                verify(adminCafePageService).updateCafePageStatus(
                        org.mockito.ArgumentMatchers.eq(targetId), captor.capture());
                assertThat(captor.getValue().getStatus()).isEqualTo(PageStatus.SUSPENDED);
            }
        }
    }

    private AdminAssistantConversation conversation(UUID owner) {
        AdminAssistantConversation newConversation = new AdminAssistantConversation();
        newConversation.setId(UUID.randomUUID());
        newConversation.setAdminUserId(owner);
        newConversation.setTitle("Ho tro kiem duyet");
        newConversation.setCreatedAt(LocalDateTime.now().minusHours(1));
        newConversation.setUpdatedAt(LocalDateTime.now().minusHours(1));
        return newConversation;
    }

    private AdminAssistantMessage message(AdminAssistantMessageRole role, String content) {
        AdminAssistantMessage newMessage = new AdminAssistantMessage();
        newMessage.setId(UUID.randomUUID());
        newMessage.setConversation(conversation);
        newMessage.setRole(role);
        newMessage.setContent(content);
        newMessage.setMetadata(Map.of());
        newMessage.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        return newMessage;
    }

    private AdminAssistantDraftAction draftAction(
            AdminAssistantDraftActionType actionType, Map<String, Object> payload) {
        AdminAssistantDraftAction newDraftAction = new AdminAssistantDraftAction();
        newDraftAction.setId(UUID.randomUUID());
        newDraftAction.setConversation(conversation);
        newDraftAction.setMessage(message(AdminAssistantMessageRole.ASSISTANT, "de xuat"));
        newDraftAction.setAdminUserId(adminUserId);
        newDraftAction.setActionType(actionType);
        newDraftAction.setPayload(payload == null ? null : new LinkedHashMap<>(payload));
        newDraftAction.setSourceRefs(List.of(Map.of("reportId", UUID.randomUUID().toString())));
        newDraftAction.setStatus(AdminAssistantDraftActionStatus.PENDING);
        newDraftAction.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        return newDraftAction;
    }

    private AdminAssistantMessageRequestDTO messageRequest(String message) {
        AdminAssistantMessageRequestDTO request = new AdminAssistantMessageRequestDTO();
        request.setMessage(message);
        return request;
    }
}
