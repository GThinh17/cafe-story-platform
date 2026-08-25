package com.cafestory.service.serviceImplement;

import com.cafestory.config.AdminAssistantSecurityProperties;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.AdminAssistantConversation;
import com.cafestory.entity.AdminAssistantDraftAction;
import com.cafestory.entity.enums.AdminAssistantDraftActionStatus;
import com.cafestory.entity.enums.AdminAssistantDraftActionType;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AdminAssistantConversationRepository;
import com.cafestory.repository.AdminAssistantDraftActionRepository;
import com.cafestory.repository.AdminAssistantMessageRepository;
import com.cafestory.service.serviceInterface.AdminBlogService;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import com.cafestory.service.serviceInterface.AdminCommentService;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.cafestory.service.serviceInterface.AdminUserService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAssistantServiceImplTest {

    private AdminAssistantConversationRepository conversationRepository;
    private AdminAssistantMessageRepository messageRepository;
    private AdminAssistantDraftActionRepository draftActionRepository;
    private AdminBlogService adminBlogService;
    private AdminAssistantServiceImpl service;

    @BeforeEach
    void setUp() {
        conversationRepository = mock(AdminAssistantConversationRepository.class);
        messageRepository = mock(AdminAssistantMessageRepository.class);
        draftActionRepository = mock(AdminAssistantDraftActionRepository.class);
        ContentReportService contentReportService = mock(ContentReportService.class);
        AdminReportAiResolutionService reportAiResolutionService = mock(AdminReportAiResolutionService.class);
        AdminReportAiAutoApplyJobService autoApplyJobService = mock(AdminReportAiAutoApplyJobService.class);
        adminBlogService = mock(AdminBlogService.class);
        AdminCommentService adminCommentService = mock(AdminCommentService.class);
        AdminUserService adminUserService = mock(AdminUserService.class);
        AdminCafePageService adminCafePageService = mock(AdminCafePageService.class);
        service = new AdminAssistantServiceImpl(
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
                new ObjectMapper(),
                "http://localhost:5678/webhook-test/cafestory-admin-assistant-chat",
                1000,
                "gpt-4o-mini",
                "http://localhost:8080/api/admin/assistant/tools",
                new AdminAssistantSecurityProperties("test-token"));
    }

    @Test
    void executeDraftAction_success_blogHideDelegatesToAdminBlogService_TC001() {
        UUID adminUserId = UUID.randomUUID();
        UUID draftActionId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        AdminAssistantConversation conversation = new AdminAssistantConversation();
        conversation.setId(conversationId);
        conversation.setAdminUserId(adminUserId);
        AdminAssistantDraftAction draftAction = new AdminAssistantDraftAction();
        draftAction.setId(draftActionId);
        draftAction.setAdminUserId(adminUserId);
        draftAction.setConversation(conversation);
        draftAction.setActionType(AdminAssistantDraftActionType.BLOG_HIDE);
        draftAction.setPayload(Map.of("blogId", blogId.toString()));
        draftAction.setStatus(AdminAssistantDraftActionStatus.PENDING);
        draftAction.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        BlogResponseDTO blogResponse = new BlogResponseDTO();
        blogResponse.setId(blogId);
        blogResponse.setStatus(PostStatus.HIDDEN);

        when(draftActionRepository.findByIdAndAdminUserId(draftActionId, adminUserId))
                .thenReturn(Optional.of(draftAction));
        when(adminBlogService.updateBlogStatus(any(), any())).thenReturn(blogResponse);
        when(draftActionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.executeDraftAction(adminUserId, draftActionId);

        assertThat(result.getStatus()).isEqualTo(AdminAssistantDraftActionStatus.EXECUTED);
        assertThat(result.getExecutionResult()).containsEntry("status", "HIDDEN");
        verify(adminBlogService).updateBlogStatus(any(), any());
    }
}
