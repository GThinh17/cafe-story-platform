package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;
import com.cafestory.dto.responseDTO.AdminDashboardSummaryResponseDTO;
import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.AdminAssistantConversation;
import com.cafestory.entity.AdminAssistantMessage;
import com.cafestory.entity.AdminAssistantToolCall;
import com.cafestory.entity.enums.AdminAssistantToolCallStatus;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.entity.enums.UserRole;
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
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        request.setInput(Map.of("query", "Cho t\u00f4i xem b\u00e1o c\u00e1o b\u00e0i vi\u1ebft \u0111ang review"));

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
        request.setInput(Map.of("query", "ng\u01b0\u1eddi d\u00f9ng b\u1ecb kh\u00f3a v\u00ec spam"));

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
        request.setInput(Map.of("query", "b\u00e0i vi\u1ebft \u0111\u00e3 \u1ea9n"));

        when(adminBlogService.getBlogs(any(), any(), any(), any())).thenReturn(emptyPage());

        AdminAssistantToolResponseDTO response = service.executeTool("search_blogs", request, adminUserId);

        assertThat(response.getToolName()).isEqualTo("search_blogs");
        verify(adminBlogService).getBlogs(
                eq(PostStatus.HIDDEN),
                isNull(),
                isNull(),
                any());
    }

    @Test
    void executeTool_searchComments_infersVietnameseRemovedStatus_TC005() {
        UUID adminUserId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("query", "b\u00ecnh lu\u1eadn b\u1ecb x\u00f3a"));

        when(adminCommentService.getComments(any(), any(), any(), any())).thenReturn(emptyPage());

        AdminAssistantToolResponseDTO response = service.executeTool("search_comments", request, adminUserId);

        assertThat(response.getToolName()).isEqualTo("search_comments");
        verify(adminCommentService).getComments(
                eq(PostStatus.REMOVED),
                isNull(),
                isNull(),
                any());
    }

    @Test
    void executeTool_dashboardSummary_success_TC006() {
        AdminDashboardSummaryResponseDTO summary = new AdminDashboardSummaryResponseDTO();
        summary.setTotalUsers(10L);
        when(adminDashboardService.getSummary()).thenReturn(summary);

        AdminAssistantToolResponseDTO response = service.executeTool("  dashboard_summary  ", null, null);

        assertThat(response.getToolName()).isEqualTo("dashboard_summary");
        assertThat(response.getMaskedFields()).isEmpty();
        assertThat(response.getSourceRefs()).hasSize(1);
        assertThat(response.getGeneratedAt()).isNotNull();
        // Không có hội thoại thì không ghi nhật ký lời gọi công cụ.
        verify(toolCallRepository, never()).save(any());
    }

    @Test
    void executeTool_getReportDetail_success_TC007() {
        UUID reportId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("reportId", reportId.toString()));
        when(contentReportService.getReport(reportId)).thenReturn(null);

        assertThat(service.executeTool("get_report_detail", request, null).getToolName())
                .isEqualTo("get_report_detail");
        verify(contentReportService).getReport(reportId);
    }

    @Test
    void executeTool_getModerationQueueSummary_success_TC008() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("status", "PENDING", "page", "2", "size", "80"));
        when(adminModerationService.getQueue(any())).thenReturn(emptyPage());
        when(adminModerationService.getJobs(any(), any())).thenReturn(emptyPage());

        assertThat(service.executeTool("get_moderation_queue_summary", request, null).getToolName())
                .isEqualTo("get_moderation_queue_summary");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminModerationService).getQueue(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        // size bị kẹp ở 50.
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void executeTool_searchBlogs_passesAuthorAndPageFilters_TC009() {
        UUID authorUserId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of(
                "status", "PUBLISHED",
                "authorUserId", authorUserId.toString(),
                "pageId", pageId.toString()));
        when(adminBlogService.getBlogs(any(), any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_blogs", request, null);

        verify(adminBlogService).getBlogs(eq(PostStatus.PUBLISHED), eq(authorUserId), eq(pageId), any());
    }

    @Test
    void executeTool_getBlogSummary_success_TC010() {
        UUID blogId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("blogId", blogId.toString()));
        when(adminBlogService.getBlog(blogId)).thenReturn(null);

        service.executeTool("get_blog_summary", request, null);

        verify(adminBlogService).getBlog(blogId);
    }

    @Test
    void executeTool_getCommentSummary_success_TC011() {
        UUID commentId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("commentId", commentId.toString()));
        when(adminCommentService.getComment(commentId)).thenReturn(null);

        service.executeTool("get_comment_summary", request, null);

        verify(adminCommentService).getComment(commentId);
    }

    @Test
    void executeTool_getCafePageSummary_success_TC012() {
        UUID pageId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("pageId", pageId.toString()));
        when(adminCafePageService.getCafePage(pageId)).thenReturn(null);

        service.executeTool("get_cafe_page_summary", request, null);

        verify(adminCafePageService).getCafePage(pageId);
    }

    @Test
    void executeTool_searchCafePages_infersSuspendedStatus_TC013() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("query", "quán bị khoá"));
        when(adminCafePageService.getCafePages(any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_cafe_pages", request, null);

        verify(adminCafePageService).getCafePages(eq(PageStatus.SUSPENDED), isNull(), any());
    }

    @Test
    void executeTool_searchCafePages_infersActiveStatus_TC014() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("message", "trang quán đang hoạt động"));
        when(adminCafePageService.getCafePages(any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_cafe_pages", request, null);

        verify(adminCafePageService).getCafePages(eq(PageStatus.ACTIVE), isNull(), any());
    }

    @Test
    void executeTool_searchCafePages_infersDraftStatus_TC015() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("query", "trang quán bản nháp"));
        when(adminCafePageService.getCafePages(any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_cafe_pages", request, null);

        verify(adminCafePageService).getCafePages(eq(PageStatus.DRAFT), isNull(), any());
    }

    @Test
    void executeTool_searchReports_infersRemainingStatuses_TC016() {
        when(contentReportService.getReports(any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_reports", requestWithQuery("báo cáo đã xử lý"), null);
        verify(contentReportService).getReports(eq(ReportStatus.RESOLVED), any(), any());

        service.executeTool("search_reports", requestWithQuery("báo cáo bị từ chối"), null);
        verify(contentReportService).getReports(eq(ReportStatus.REJECTED), any(), any());

        service.executeTool("search_reports", requestWithQuery("báo cáo chưa xử lý"), null);
        verify(contentReportService).getReports(eq(ReportStatus.OPEN), any(), any());
    }

    @Test
    void executeTool_searchReports_infersRemainingTargetTypes_TC017() {
        when(contentReportService.getReports(any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_reports", requestWithQuery("báo cáo bình luận"), null);
        verify(contentReportService).getReports(any(), eq(ReportTargetType.COMMENT), any());

        service.executeTool("search_reports", requestWithQuery("báo cáo tài khoản"), null);
        verify(contentReportService).getReports(any(), eq(ReportTargetType.USER), any());

        service.executeTool("search_reports", requestWithQuery("báo cáo trang quán"), null);
        verify(contentReportService).getReports(any(), eq(ReportTargetType.CAFE_PAGE), any());
    }

    @Test
    void executeTool_searchBlogs_infersRemainingPostStatuses_TC018() {
        when(adminBlogService.getBlogs(any(), any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_blogs", requestWithQuery("bài viết bản nháp"), null);
        verify(adminBlogService).getBlogs(eq(PostStatus.DRAFT), isNull(), isNull(), any());

        service.executeTool("search_blogs", requestWithQuery("bài viết công khai"), null);
        verify(adminBlogService).getBlogs(eq(PostStatus.PUBLISHED), isNull(), isNull(), any());
    }

    @Test
    void executeTool_searchUsers_infersActiveAccountAndCompactsQuery_TC019() {
        when(adminUserService.getUsers(any(), any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_users", requestWithQuery("tìm người dùng luan123 đang hoạt động"), null);

        verify(adminUserService).getUsers(eq("luan123 hoat dong"), eq(true), isNull(), any());
    }

    @Test
    void executeTool_searchUsers_keepsExplicitSearchValue_TC020() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("query", "tìm người dùng", "search", "luan", "role", "REVIEWER"));
        when(adminUserService.getUsers(any(), any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_users", request, null);

        verify(adminUserService).getUsers(eq("luan"), isNull(), eq(UserRole.REVIEWER), any());
    }

    @Test
    void executeTool_searchUsers_queryOfOnlyGenericTermsKeepsWholeQuery_TC021() {
        when(adminUserService.getUsers(any(), any(), any(), any())).thenReturn(emptyPage());

        service.executeTool("search_users", requestWithQuery("cho tôi xem người dùng"), null);

        verify(adminUserService).getUsers(eq("cho toi xem nguoi dung"), isNull(), isNull(), any());
    }

    @Test
    void executeTool_fail_unsupportedToolNameIsAudited_TC022() {
        UUID adminUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        AdminAssistantConversation conversation = new AdminAssistantConversation();
        conversation.setId(conversationId);
        conversation.setAdminUserId(adminUserId);
        AdminAssistantMessage message = new AdminAssistantMessage();
        message.setId(messageId);
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setConversationId(conversationId);
        request.setMessageId(messageId);
        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.executeTool("khong_ton_tai", request, adminUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported assistant tool");

        ArgumentCaptor<AdminAssistantToolCall> captor = ArgumentCaptor.forClass(AdminAssistantToolCall.class);
        verify(toolCallRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AdminAssistantToolCallStatus.FAILED);
        assertThat(captor.getValue().getErrorMessage()).contains("Unsupported assistant tool");
        assertThat(captor.getValue().getMessage()).isSameAs(message);
    }

    @Test
    void executeTool_fail_toolNameIsBlank_TC023() {
        assertThatThrownBy(() -> service.executeTool("   ", null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("toolName is required");
        assertThatThrownBy(() -> service.executeTool(null, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("toolName is required");
    }

    @Test
    void executeTool_fail_requiredIdMissingOrMalformed_TC024() {
        AdminAssistantToolRequestDTO empty = new AdminAssistantToolRequestDTO();
        assertThatThrownBy(() -> service.executeTool("get_report_detail", empty, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("reportId is required");

        AdminAssistantToolRequestDTO malformed = new AdminAssistantToolRequestDTO();
        malformed.setInput(Map.of("reportId", "khong-phai-uuid"));
        assertThatThrownBy(() -> service.executeTool("get_report_detail", malformed, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("reportId must be a valid UUID");
    }

    @Test
    void executeTool_fail_enumValueIsInvalid_TC025() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("status", "KHONG_CO"));

        assertThatThrownBy(() -> service.executeTool("search_blogs", request, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("status is invalid");
    }

    @Test
    void executeTool_success_ignoresUnknownConversationAndMessage_TC026() {
        UUID adminUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setConversationId(conversationId);
        request.setMessageId(messageId);
        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.empty());
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());
        when(adminDashboardService.getSummary()).thenReturn(new AdminDashboardSummaryResponseDTO());

        service.executeTool("dashboard_summary", request, adminUserId);

        verify(toolCallRepository, never()).save(any());
    }

    @Test
    void executeTool_success_masksShortEmailAndPhone_TC027() {
        UUID adminUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AdminAssistantConversation conversation = new AdminAssistantConversation();
        conversation.setId(conversationId);
        conversation.setAdminUserId(adminUserId);
        AdminUserResponseDTO user = new AdminUserResponseDTO();
        user.setUserId(userId);
        user.setUserEmail("@x.vn");
        user.setUserPhone(12L);
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setConversationId(conversationId);
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("userId", userId.toString()));
        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.of(conversation));
        when(adminUserService.getUser(userId)).thenReturn(user);

        AdminAssistantToolResponseDTO response = service.executeTool("get_user_summary", request, adminUserId);

        assertThat(response.getData().toString()).contains("***");
        assertThat(response.getData().toString()).contains("****");
    }

    @Test
    void executeTool_success_pagedResultRecordsContentSize_TC028() {
        UUID adminUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        AdminAssistantConversation conversation = new AdminAssistantConversation();
        conversation.setId(conversationId);
        conversation.setAdminUserId(adminUserId);
        AdminUserResponseDTO user = new AdminUserResponseDTO();
        user.setUserId(UUID.randomUUID());
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setConversationId(conversationId);
        request.setAdminUserId(adminUserId);
        request.setInput(Map.of("page", "khong-phai-so", "size", "  "));
        when(conversationRepository.findByIdAndAdminUserId(conversationId, adminUserId))
                .thenReturn(Optional.of(conversation));
        when(adminUserService.getUsers(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1));

        service.executeTool("search_users", request, adminUserId);

        ArgumentCaptor<AdminAssistantToolCall> captor = ArgumentCaptor.forClass(AdminAssistantToolCall.class);
        verify(toolCallRepository).save(captor.capture());
        assertThat(captor.getValue().getOutputSummary()).containsEntry("contentSize", 1);
        assertThat(captor.getValue().getStatus()).isEqualTo(AdminAssistantToolCallStatus.SUCCESS);
        assertThat(captor.getValue().getDurationMs()).isNotNegative();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminUserService).getUsers(any(), any(), any(), pageableCaptor.capture());
        // Tham số phân trang không đọc được thì rơi về mặc định 0/10.
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    private AdminAssistantToolRequestDTO requestWithQuery(String query) {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setInput(Map.of("query", query));
        return request;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Page emptyPage() {
        return new PageImpl(List.of(), PageRequest.of(0, 5), 0);
    }
}
