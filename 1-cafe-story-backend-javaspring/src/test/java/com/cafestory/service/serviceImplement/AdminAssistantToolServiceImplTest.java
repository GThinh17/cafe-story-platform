package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.AdminAssistantConversation;
import com.cafestory.repository.AdminAssistantConversationRepository;
import com.cafestory.repository.AdminAssistantMessageRepository;
import com.cafestory.repository.AdminAssistantToolCallRepository;
import com.cafestory.service.serviceInterface.AdminBlogService;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import com.cafestory.service.serviceInterface.AdminCommentService;
import com.cafestory.service.serviceInterface.AdminDashboardService;
import com.cafestory.service.serviceInterface.AdminModerationService;
import com.cafestory.service.serviceInterface.AdminUserService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAssistantToolServiceImplTest {

    private AdminDashboardService adminDashboardService;
    private ContentReportService contentReportService;
    private AdminModerationService adminModerationService;
    private AdminUserService adminUserService;
    private AdminBlogService adminBlogService;
    private AdminCommentService adminCommentService;
    private AdminCafePageService adminCafePageService;
    private AdminAssistantConversationRepository conversationRepository;
    private AdminAssistantMessageRepository messageRepository;
    private AdminAssistantToolCallRepository toolCallRepository;
    private AdminAssistantToolServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDashboardService = mock(AdminDashboardService.class);
        contentReportService = mock(ContentReportService.class);
        adminModerationService = mock(AdminModerationService.class);
        adminUserService = mock(AdminUserService.class);
        adminBlogService = mock(AdminBlogService.class);
        adminCommentService = mock(AdminCommentService.class);
        adminCafePageService = mock(AdminCafePageService.class);
        conversationRepository = mock(AdminAssistantConversationRepository.class);
        messageRepository = mock(AdminAssistantMessageRepository.class);
        toolCallRepository = mock(AdminAssistantToolCallRepository.class);
        service = new AdminAssistantToolServiceImpl(
                adminDashboardService,
                contentReportService,
                adminModerationService,
                adminUserService,
                adminBlogService,
                adminCommentService,
                adminCafePageService,
                conversationRepository,
                messageRepository,
                toolCallRepository,
                new ObjectMapper());
    }

    @Test
    void executeTool_success_masksSensitiveUserFieldsAndAudits_TC001() {
        UUID adminUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AdminAssistantConversation conversation = new AdminAssistantConversation();
        conversation.setId(conversationId);
        conversation.setAdminUserId(adminUserId);
        AdminUserResponseDTO user = new AdminUserResponseDTO();
        user.setUserId(userId);
        user.setUserName("admin-target");
        user.setUserEmail("target@example.com");
        user.setUserPhone(912345678L);
        user.setAccountStatus(true);
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setConversationId(conversationId);
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("userId", userId.toString()));

        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.of(conversation));
        when(adminUserService.getUser(userId)).thenReturn(user);

        AdminAssistantToolResponseDTO response = service.executeTool("get_user_summary", request, adminUserId);

        assertThat(response.getToolName()).isEqualTo("get_user_summary");
        assertThat(response.getMaskedFields()).anyMatch(field -> field.contains("userEmail"));
        assertThat(response.getMaskedFields()).anyMatch(field -> field.contains("userPhone"));
        assertThat(response.getData().toString()).doesNotContain("target@example.com");
        verify(toolCallRepository).save(any());
    }
}
