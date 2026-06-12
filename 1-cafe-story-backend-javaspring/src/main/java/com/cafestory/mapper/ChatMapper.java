package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.chat.ChatMemberResponseDTO;
import com.cafestory.dto.responseDTO.chat.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.chat.ConversationResponseDTO;
import com.cafestory.entity.ChatMember;
import com.cafestory.entity.ChatMessage;
import com.cafestory.entity.Conversation;
import com.cafestory.entity.enums.ConversationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ChatMapper {

    public ConversationResponseDTO toConversationResponseDTO(
            Conversation conversation,
            List<ChatMember> members,
            UUID viewerUserId,
            ChatMessage latestMessage,
            long unreadCount) {
        ConversationResponseDTO response = new ConversationResponseDTO();
        response.setId(conversation.getId());
        response.setType(conversation.getType());
        response.setGroupName(conversation.getGroupName());
        response.setGroupAvatar(conversation.getGroupAvatar());
        response.setChatName(resolveChatName(conversation, members, viewerUserId));
        response.setUserName(resolveUserName(conversation, members, viewerUserId));
        response.setChatAvatar(resolveChatAvatar(conversation, members, viewerUserId));
        response.setLatestMessageId(conversation.getLatestMessageId());
        response.setLatestMessagePreview(conversation.getLatestMessagePreview());
        response.setLastMessage(conversation.getLatestMessagePreview());
        response.setLastMessageAt(latestMessage != null ? latestMessage.getCreatedAt() : conversation.getUpdatedAt());
        response.setRead(isConversationRead(latestMessage, viewerUserId));
        response.setUnreadCount(unreadCount);
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        response.setMembers(members.stream().map(this::toChatMemberResponseDTO).toList());
        return response;
    }

    public ChatMemberResponseDTO toChatMemberResponseDTO(ChatMember member) {
        ChatMemberResponseDTO response = new ChatMemberResponseDTO();
        response.setId(member.getId());
        response.setUserId(member.getUser().getUserId());
        response.setUserName(member.getUser().getUserName());
        response.setUserFullName(member.getUser().getUserFullName());
        response.setUserAvatar(member.getUser().getUserAvatar());
        response.setRole(member.getRole());
        response.setJoinedAt(member.getJoinedAt());
        return response;
    }

    public ChatMessageResponseDTO toChatMessageResponseDTO(ChatMessage message) {
        ChatMessageResponseDTO response = new ChatMessageResponseDTO();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSender().getUserId());
        response.setType(message.getType());
        response.setText(message.getText());
        response.setImageUrls(message.getImageUrls());
        response.setStickerUrl(message.getStickerUrl());
        response.setStickerId(message.getStickerId());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        response.setStatus(message.getStatus());
        response.setRead(message.isRead());
        response.setReadAt(message.getReadAt());
        return response;
    }

    private String resolveChatName(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() == ConversationType.GROUP) {
            return conversation.getGroupName();
        }

        return otherMember(members, viewerUserId)
                .map(member -> displayName(member.getUser().getUserFullName(), member.getUser().getUserName()))
                .orElse(null);
    }

    private String resolveUserName(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() == ConversationType.GROUP) {
            return conversation.getGroupName();
        }

        return otherMember(members, viewerUserId)
                .map(member -> member.getUser().getUserName())
                .orElse(null);
    }

    private String resolveChatAvatar(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() == ConversationType.GROUP) {
            return conversation.getGroupAvatar();
        }

        return otherMember(members, viewerUserId)
                .map(member -> member.getUser().getUserAvatar())
                .orElse(null);
    }

    private java.util.Optional<ChatMember> otherMember(List<ChatMember> members, UUID viewerUserId) {
        if (viewerUserId == null) {
            return members.stream().findFirst();
        }

        return members.stream()
                .filter(member -> !viewerUserId.equals(member.getUser().getUserId()))
                .findFirst();
    }

    private String displayName(String fullName, String userName) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return userName;
    }

    private boolean isConversationRead(ChatMessage latestMessage, UUID viewerUserId) {
        if (latestMessage == null) {
            return true;
        }
        if (viewerUserId != null && viewerUserId.equals(latestMessage.getSender().getUserId())) {
            return true;
        }
        return latestMessage.isRead();
    }
}
