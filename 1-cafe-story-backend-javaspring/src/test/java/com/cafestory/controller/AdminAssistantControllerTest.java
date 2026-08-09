package com.cafestory.controller;

import com.cafestory.config.AdminAssistantSecurityProperties;
import com.cafestory.dto.requestDTO.AdminAssistantConversationCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminAssistantMessageRequestDTO;
import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantChatResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantConversationResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantDraftActionResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantMessageResponseDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;
import com.cafestory.service.serviceInterface.AdminAssistantService;
import com.cafestory.service.serviceInterface.AdminAssistantToolService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminAssistantController}.
 *
 * <p>Điểm an ninh cần canh: endpoint {@code /tools/**} do n8n gọi, chỉ chấp nhận
 * đúng token cấu hình; sai hoặc thiếu token phải trả 401 và không đụng tới
 * service.
 */
@ExtendWith(MockitoExtension.class)
class AdminAssistantControllerTest {

    private static final String TOOL_TOKEN = "tool-token-secret";

    @Mock
    private AdminAssistantService assistantService;
    @Mock
    private AdminAssistantToolService toolService;

    private AdminAssistantController controller;
    private AuthenticatedUserPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new AdminAssistantController(
                assistantService, toolService, new AdminAssistantSecurityProperties(TOOL_TOKEN));
        principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "admin", List.of("ADMIN"));
    }

    @Test
    void createConversation_success_delegates_TC001() {
        AdminAssistantConversationCreateRequestDTO request = new AdminAssistantConversationCreateRequestDTO();
        AdminAssistantConversationResponseDTO response = new AdminAssistantConversationResponseDTO();
        when(assistantService.createConversation(principal.userId(), request)).thenReturn(response);

        assertThat(controller.createConversation(request, principal)).isSameAs(response);
    }

    @Test
    void getConversations_success_normalizesPagingAndSortsByUpdatedAt_TC002() {
        Page<AdminAssistantConversationResponseDTO> page = new PageImpl<>(List.of());
        when(assistantService.getConversations(eq(principal.userId()), any(Pageable.class))).thenReturn(page);

        assertThat(controller.getConversations(-1, 500, principal)).isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assistantService).getConversations(eq(principal.userId()), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getSort().getOrderFor("updatedAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getMessages_success_sortsByCreatedAt_TC003() {
        UUID conversationId = UUID.randomUUID();
        Page<AdminAssistantMessageResponseDTO> page = new PageImpl<>(List.of());
        when(assistantService.getMessages(eq(principal.userId()), eq(conversationId), any(Pageable.class)))
                .thenReturn(page);

        assertThat(controller.getMessages(conversationId, 0, 0, principal)).isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assistantService).getMessages(eq(principal.userId()), eq(conversationId), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void sendMessage_success_delegates_TC004() {
        UUID conversationId = UUID.randomUUID();
        AdminAssistantMessageRequestDTO request = new AdminAssistantMessageRequestDTO();
        AdminAssistantChatResponseDTO response = new AdminAssistantChatResponseDTO();
        when(assistantService.sendMessage(principal.userId(), conversationId, request)).thenReturn(response);

        assertThat(controller.sendMessage(conversationId, request, principal)).isSameAs(response);
    }

    @Test
    void streamMessage_success_delegates_TC005() {
        UUID conversationId = UUID.randomUUID();
        AdminAssistantMessageRequestDTO request = new AdminAssistantMessageRequestDTO();
        SseEmitter emitter = new SseEmitter();
        when(assistantService.streamMessage(principal.userId(), conversationId, request)).thenReturn(emitter);

        assertThat(controller.streamMessage(conversationId, request, principal)).isSameAs(emitter);
    }

    @Test
    void getDraftAction_success_delegates_TC006() {
        UUID draftActionId = UUID.randomUUID();
        AdminAssistantDraftActionResponseDTO response = new AdminAssistantDraftActionResponseDTO();
        when(assistantService.getDraftAction(principal.userId(), draftActionId)).thenReturn(response);

        assertThat(controller.getDraftAction(draftActionId, principal)).isSameAs(response);
    }

    @Test
    void executeDraftAction_success_delegates_TC007() {
        UUID draftActionId = UUID.randomUUID();
        AdminAssistantDraftActionResponseDTO response = new AdminAssistantDraftActionResponseDTO();
        when(assistantService.executeDraftAction(principal.userId(), draftActionId)).thenReturn(response);

        assertThat(controller.executeDraftAction(draftActionId, principal)).isSameAs(response);
    }

    @Test
    void executeTool_success_withMatchingToolToken_TC008() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();
        request.setAdminUserId(principal.userId());
        AdminAssistantToolResponseDTO response = new AdminAssistantToolResponseDTO();
        when(toolService.executeTool("listReports", request, principal.userId())).thenReturn(response);

        assertThat(controller.executeTool("listReports", TOOL_TOKEN, request)).isSameAs(response);
    }

    @Test
    void executeTool_success_nullBodyPassesNullAdminUserId_TC009() {
        AdminAssistantToolResponseDTO response = new AdminAssistantToolResponseDTO();
        when(toolService.executeTool("listReports", null, null)).thenReturn(response);

        assertThat(controller.executeTool("listReports", TOOL_TOKEN, null)).isSameAs(response);
    }

    @Test
    void executeTool_fail_wrongOrMissingToolToken_TC010() {
        AdminAssistantToolRequestDTO request = new AdminAssistantToolRequestDTO();

        assertThatThrownBy(() -> controller.executeTool("listReports", "sai-token", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Assistant tool token is invalid")
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        assertThatThrownBy(() -> controller.executeTool("listReports", null, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Assistant tool token is invalid");

        verifyNoInteractions(toolService);
    }

    @Test
    void createConversation_fail_missingPrincipal_TC011() {
        assertThatThrownBy(() -> controller.createConversation(null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }
}
