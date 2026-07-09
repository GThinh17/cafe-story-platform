package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.AdminAssistantConversation;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Test
    void executeTool_searchReports_infersVietnameseStatusAndTargetType_TC002() {
        UUID adminUserId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("query", "Cho tôi xem báo cáo bài viết đang review"));

        when(contentReportService.getReports(any(), any(), any())).thenReturn(emptyPage());

        AdminAssistantToolResponseDTO response = service.executeTool("search_reports", request, adminUserId);

        assertThat(response.getToolName()).isEqualTo("search_reports");
        verify(contentReportService).getReports(
                eq(ReportStatus.REVIEWING),
                eq(ReportTargetType.BLOG),
                any());
    }

    @Test
    void executeTool_searchUsers_infersVietnameseInactiveAccount_TC003() {
        UUID adminUserId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("query", "người dùng bị khóa vì spam"));

        when(adminUserService.getUsers(any(), any(), any(), any())).thenReturn(emptyPage());

        AdminAssistantToolResponseDTO response = service.executeTool("search_users", request, adminUserId);

        assertThat(response.getToolName()).isEqualTo("search_users");
        verify(adminUserService).getUsers(
                eq("spam"),
                eq(false),
                isNull(),
                any());
    }

    @Test
    void executeTool_searchBlogs_infersVietnameseHiddenStatus_TC004() {
        UUID adminUserId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("query", "bài viết đã ẩn"));

        when(adminBlogService.getBlogs(any(), any(), any(), any())).thenReturn(emptyPage());

        AdminAssistantToolResponseDTO response = service.executeTool("search_blogs", request, adminUserId);

        assertThat(response.getToolName()).isEqualTo("search_blogs");
        verify(adminBlogService).getBlogs(
                eq(PostStatus.HIDDEN),
                isNull(),
                isNull(),
                any());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Page emptyPage() {
        return new PageImpl(List.of(), PageRequest.of(0, 5), 0);
    }
}
