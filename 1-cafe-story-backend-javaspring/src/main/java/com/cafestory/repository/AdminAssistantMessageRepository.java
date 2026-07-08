package com.cafestory.repository;

import com.cafestory.entity.AdminAssistantMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminAssistantMessageRepository extends JpaRepository<AdminAssistantMessage, UUID> {

    Page<AdminAssistantMessage> findByConversationId(UUID conversationId, Pageable pageable);

    List<AdminAssistantMessage> findTop10ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
