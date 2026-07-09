package com.cafestory.repository;

import com.cafestory.entity.AdminAssistantDraftAction;
import com.cafestory.entity.enums.AdminAssistantDraftActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminAssistantDraftActionRepository extends JpaRepository<AdminAssistantDraftAction, UUID> {

    Optional<AdminAssistantDraftAction> findByIdAndAdminUserId(UUID id, UUID adminUserId);

    long countByConversationIdAndStatus(UUID conversationId, AdminAssistantDraftActionStatus status);
}
