package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AddMemberRequestDTO;
import com.cafestory.dto.requestDTO.CreateCafePageConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateDirectConversationRequestDTO;
import com.cafestory.dto.requestDTO.CreateGroupConversationRequestDTO;
import com.cafestory.dto.requestDTO.SendMessageRequestDTO;
import com.cafestory.dto.requestDTO.UpdateGroupInfoRequestDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;
import com.cafestory.service.serviceInterface.ChatService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link ChatController}.
 *
 * <p>Điểm cần canh: bộ điều khiển luôn ghi đè định danh người gửi bằng id lấy từ
 * token, không tin giá trị trong thân yêu cầu — nếu không, người dùng có thể gửi
 * tin nhắn dưới danh nghĩa người khác.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private AuthenticatedUserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "an", List.of("USER"));
    }

    @Test
    void createOrGetDirectConversation_success_overridesFirstUserIdFromToken_TC001() {
        CreateDirectConversationRequestDTO request = new CreateDirectConversationRequestDTO();
        request.setFirstUserId(UUID.randomUUID());
        request.setSecondUserId(UUID.randomUUID());
        ConversationResponseDTO response = new ConversationResponseDTO();
        when(chatService.createOrGetDirectConversation(request)).thenReturn(response);

        assertThat(chatController.createOrGetDirectConversation(request, principal)).isSameAs(response);
        assertThat(request.getFirstUserId()).isEqualTo(principal.userId());
    }

    @Test
    void createOrGetCafePageConversation_success_overridesUserIdFromToken_TC002() {
        CreateCafePageConversationRequestDTO request = new CreateCafePageConversationRequestDTO();
        request.setCafePageId(UUID.randomUUID());
        ConversationResponseDTO response = new ConversationResponseDTO();
        when(chatService.createOrGetCafePageConversation(request)).thenReturn(response);

        assertThat(chatController.createOrGetCafePageConversation(request, principal)).isSameAs(response);
        assertThat(request.getUserId()).isEqualTo(principal.userId());
    }

    @Test
    void createGroupConversation_success_overridesCreatorFromToken_TC003() {
        CreateGroupConversationRequestDTO request = new CreateGroupConversationRequestDTO();
        request.setGroupName("Nhom");
        request.setMemberIds(List.of(UUID.randomUUID()));
        ConversationResponseDTO response = new ConversationResponseDTO();
        when(chatService.createGroupConversation(request)).thenReturn(response);

        assertThat(chatController.createGroupConversation(request, principal)).isSameAs(response);
        assertThat(request.getCreatorUserId()).isEqualTo(principal.userId());
    }

    @Test
    void getUserConversations_success_delegates_TC004() {
        List<ConversationResponseDTO> conversations = List.of(new ConversationResponseDTO());
        when(chatService.getUserConversations(principal.userId())).thenReturn(conversations);

        assertThat(chatController.getUserConversations(principal)).isSameAs(conversations);
    }

    @Test
    void getCafePageConversations_success_delegates_TC005() {
        UUID cafePageId = UUID.randomUUID();
        List<ConversationResponseDTO> conversations = List.of(new ConversationResponseDTO());
        when(chatService.getCafePageConversations(cafePageId, principal.userId())).thenReturn(conversations);

        assertThat(chatController.getCafePageConversations(cafePageId, principal)).isSameAs(conversations);
    }

    @Test
    void getMessagesByConversationId_success_passesPagingThrough_TC006() {
        UUID conversationId = UUID.randomUUID();
        List<ChatMessageResponseDTO> messages = List.of(new ChatMessageResponseDTO());
        when(chatService.getMessagesByConversationId(conversationId, principal.userId(), 2, 30))
                .thenReturn(messages);

        assertThat(chatController.getMessagesByConversationId(conversationId, principal, 2, 30))
                .isSameAs(messages);
    }

    @Test
    void sendMessage_success_overridesSenderFromToken_TC007() {
        UUID conversationId = UUID.randomUUID();
        SendMessageRequestDTO request = new SendMessageRequestDTO();
        request.setSenderId(UUID.randomUUID());
        ChatMessageResponseDTO response = new ChatMessageResponseDTO();
        when(chatService.sendMessage(conversationId, request)).thenReturn(response);

        assertThat(chatController.sendMessage(conversationId, request, principal)).isSameAs(response);
        assertThat(request.getSenderId()).isEqualTo(principal.userId());
    }

    @Test
    void addMember_success_delegates_TC008() {
        UUID conversationId = UUID.randomUUID();
        AddMemberRequestDTO request = new AddMemberRequestDTO();
        request.setMemberUserId(UUID.randomUUID());
        ConversationResponseDTO response = new ConversationResponseDTO();
        when(chatService.addMember(conversationId, principal.userId(), request.getMemberUserId()))
                .thenReturn(response);

        assertThat(chatController.addMember(conversationId, request, principal)).isSameAs(response);
    }

    @Test
    void removeMember_success_delegates_TC009() {
        UUID conversationId = UUID.randomUUID();
        UUID memberUserId = UUID.randomUUID();
        ConversationResponseDTO response = new ConversationResponseDTO();
        when(chatService.removeMember(conversationId, principal.userId(), memberUserId)).thenReturn(response);

        assertThat(chatController.removeMember(conversationId, memberUserId, principal)).isSameAs(response);
    }

    @Test
    void leaveGroup_success_delegates_TC010() {
        UUID conversationId = UUID.randomUUID();

        chatController.leaveGroup(conversationId, principal);

        verify(chatService).leaveGroup(conversationId, principal.userId());
    }

    @Test
    void updateGroupInfo_success_overridesActorFromToken_TC011() {
        UUID conversationId = UUID.randomUUID();
        UpdateGroupInfoRequestDTO request = new UpdateGroupInfoRequestDTO();
        request.setActorUserId(UUID.randomUUID());
        ConversationResponseDTO response = new ConversationResponseDTO();
        when(chatService.updateGroupInfo(conversationId, request)).thenReturn(response);

        assertThat(chatController.updateGroupInfo(conversationId, request, principal)).isSameAs(response);
        assertThat(request.getActorUserId()).isEqualTo(principal.userId());
    }

    @Test
    void getUserConversations_fail_missingPrincipal_TC012() {
        assertThatThrownBy(() -> chatController.getUserConversations(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }
}
