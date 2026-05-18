package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.chat.ChatMemberResponseDTO;
import com.cafestory.dto.responseDTO.chat.ChatMessageResponseDTO;
import com.cafestory.dto.responseDTO.chat.ConversationResponseDTO;
import com.cafestory.entity.ChatMember;
import com.cafestory.entity.ChatMessage;
import com.cafestory.entity.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {

    public ConversationResponseDTO toConversationResponseDTO(Conversation conversation, List<ChatMember> members) {
        ConversationResponseDTO response = new ConversationResponseDTO();
        response.setId(conversation.getId());
        response.setType(conversation.getType());
        response.setGroupName(conversation.getGroupName());
        response.setGroupAvatar(conversation.getGroupAvatar());
        response.setLatestMessageId(conversation.getLatestMessageId());
        response.setLatestMessagePreview(conversation.getLatestMessagePreview());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        response.setMembers(members.stream().map(this::toChatMemberResponseDTO).toList());
        return response;
    }

    public ChatMemberResponseDTO toChatMemberResponseDTO(ChatMember member) {
        ChatMemberResponseDTO response = new ChatMemberResponseDTO();
        response.setId(member.getId());
        response.setUserId(member.getUser().getUserId());
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
        return response;
    }
}
