package com.cafestory.repository;

import com.cafestory.entity.AdminAssistantConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminAssistantConversationRepository extends JpaRepository<AdminAssistantConversation, UUID> {

    Page<AdminAssistantConversation> findByAdminUserId(UUID adminUserId, Pageable pageable);

    Optional<AdminAssistantConversation> findByIdAndAdminUserId(UUID id, UUID adminUserId);
}
