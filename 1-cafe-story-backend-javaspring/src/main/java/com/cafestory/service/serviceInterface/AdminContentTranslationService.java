package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminContentTranslationRequestDTO;
import com.cafestory.dto.responseDTO.AdminContentTranslationResponseDTO;

public interface AdminContentTranslationService {
    AdminContentTranslationResponseDTO translate(AdminContentTranslationRequestDTO request);
}
