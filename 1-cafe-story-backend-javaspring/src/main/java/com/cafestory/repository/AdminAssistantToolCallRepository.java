package com.cafestory.repository;

import com.cafestory.entity.AdminAssistantToolCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAssistantToolCallRepository extends JpaRepository<AdminAssistantToolCall, UUID> {
}
