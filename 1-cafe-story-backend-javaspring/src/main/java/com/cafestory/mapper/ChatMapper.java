package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.ChatMemberResponseDTO;
import com.cafestory.dto.responseDTO.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.ConversationResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.ChatMember;
import com.cafestory.entity.ChatMessage;
import com.cafestory.entity.Conversation;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ChatSenderContextType;
import com.cafestory.entity.enums.ChatTargetType;
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
        response.setTargetType(resolveTargetType(conversation));
        response.setTargetId(resolveTargetId(conversation, members, viewerUserId));
        response.setTargetUserId(resolveTargetUserId(conversation, members, viewerUserId));
        response.setTargetCafePageId(resolveTargetCafePageId(conversation));
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
        response.setSenderContextType(message.getSenderContextType());
        response.setSenderCafePageId(resolveSenderCafePageId(message));
        response.setSenderDisplayName(resolveSenderDisplayName(message));
        response.setSenderAvatar(resolveSenderAvatar(message));
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
        if (conversation.getType() == ConversationType.CAFE_PAGE) {
            CafePage cafePage = conversation.getCafePage();
            return cafePage == null ? null : cafePage.getName();
        }

        return otherMember(members, viewerUserId)
                .map(member -> displayName(member.getUser().getUserFullName(), member.getUser().getUserName()))
                .orElse(null);
    }

    private String resolveUserName(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() == ConversationType.GROUP) {
            return conversation.getGroupName();
        }
        if (conversation.getType() == ConversationType.CAFE_PAGE) {
            CafePage cafePage = conversation.getCafePage();
            return cafePage == null ? null : cafePage.getName();
        }

        return otherMember(members, viewerUserId)
                .map(member -> member.getUser().getUserName())
                .orElse(null);
    }

    private String resolveChatAvatar(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() == ConversationType.GROUP) {
            return conversation.getGroupAvatar();
        }
        if (conversation.getType() == ConversationType.CAFE_PAGE) {
            CafePage cafePage = conversation.getCafePage();
            return cafePage == null ? null : cafePage.getAvatarUrl();
        }

        return otherMember(members, viewerUserId)
                .map(member -> member.getUser().getUserAvatar())
                .orElse(null);
    }

    private UUID resolveSenderCafePageId(ChatMessage message) {
        CafePage cafePage = message.getSenderCafePage();
        return cafePage == null ? null : cafePage.getId();
    }

    private String resolveSenderDisplayName(ChatMessage message) {
        if (message.getSenderContextType() == ChatSenderContextType.CAFE_PAGE && message.getSenderCafePage() != null) {
            return message.getSenderCafePage().getName();
        }
        User sender = message.getSender();
        return displayName(sender.getUserFullName(), sender.getUserName());
    }

    private String resolveSenderAvatar(ChatMessage message) {
        if (message.getSenderContextType() == ChatSenderContextType.CAFE_PAGE && message.getSenderCafePage() != null) {
            return message.getSenderCafePage().getAvatarUrl();
        }
        return message.getSender().getUserAvatar();
    }

    private ChatTargetType resolveTargetType(Conversation conversation) {
        if (conversation.getType() == ConversationType.GROUP) {
            return ChatTargetType.GROUP;
        }
        if (conversation.getType() == ConversationType.CAFE_PAGE) {
            return ChatTargetType.CAFE_PAGE;
        }
        return ChatTargetType.USER;
    }

    private UUID resolveTargetId(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() == ConversationType.GROUP) {
            return conversation.getId();
        }
        if (conversation.getType() == ConversationType.CAFE_PAGE) {
            return resolveTargetCafePageId(conversation);
        }
        return resolveTargetUserId(conversation, members, viewerUserId);
    }

    private UUID resolveTargetUserId(Conversation conversation, List<ChatMember> members, UUID viewerUserId) {
        if (conversation.getType() != ConversationType.DIRECT) {
            return null;
        }
        return otherMember(members, viewerUserId)
                .map(member -> member.getUser().getUserId())
                .orElse(null);
    }

    private UUID resolveTargetCafePageId(Conversation conversation) {
        CafePage cafePage = conversation.getCafePage();
        return cafePage == null ? null : cafePage.getId();
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
