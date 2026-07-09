package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminAssistantToolRequestDTO;
import com.cafestory.dto.responseDTO.AdminAssistantToolResponseDTO;

import java.util.UUID;

public interface AdminAssistantToolService {

    AdminAssistantToolResponseDTO executeTool(String toolName, AdminAssistantToolRequestDTO request, UUID fallbackAdminUserId);
}
