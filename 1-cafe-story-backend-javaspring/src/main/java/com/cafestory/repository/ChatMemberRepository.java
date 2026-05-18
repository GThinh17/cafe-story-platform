package com.cafestory.repository;

import com.cafestory.entity.ChatMember;
import com.cafestory.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {

    boolean existsByConversationIdAndUserUserId(UUID conversationId, UUID userId);

    Optional<ChatMember> findByConversationIdAndUserUserId(UUID conversationId, UUID userId);

    List<ChatMember> findByConversationId(UUID conversationId);

    List<ChatMember> findByUserUserId(UUID userId);

    long countByConversation(Conversation conversation);
}
