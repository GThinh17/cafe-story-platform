package com.cafestory.controller;

import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.service.serviceInterface.AdminModerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminModerationControllerTest {

    @Mock
    private AdminModerationService adminModerationService;

    @InjectMocks
    private AdminModerationController adminModerationController;

    @Test
    void getAllResults_success_TC001() {
        Page<AdminModerationResultResponseDTO> response = new PageImpl<>(List.of(resultResponse()));

        when(adminModerationService.getAllResults(
                isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(response);

        Page<AdminModerationResultResponseDTO> result =
                adminModerationController.getAllResults(null, null, null, 0, 20);

        assertThat(result).isEqualTo(response);
        verify(adminModerationService).getAllResults(
                isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getAllResults_withFilters_TC002() {
        Page<AdminModerationResultResponseDTO> response = new PageImpl<>(List.of(resultResponse()));

        when(adminModerationService.getAllResults(
                eq("SEND_ADMIN"),
                eq(ModerationDecision.VIOLATION),
                eq(Boolean.FALSE),
                any(Pageable.class))).thenReturn(response);

        Page<AdminModerationResultResponseDTO> result = adminModerationController.getAllResults(
                "SEND_ADMIN",
                ModerationDecision.VIOLATION,
                Boolean.FALSE,
                0,
                20);

        assertThat(result).isEqualTo(response);
        verify(adminModerationService).getAllResults(
                eq("SEND_ADMIN"),
                eq(ModerationDecision.VIOLATION),
                eq(Boolean.FALSE),
                any(Pageable.class));
    }

    private AdminModerationResultResponseDTO resultResponse() {
        AdminModerationResultResponseDTO response = new AdminModerationResultResponseDTO();
        response.setId(UUID.randomUUID());
        response.setBlogId(UUID.randomUUID());
        response.setCaptionScore(12);
        response.setImageScore(86);
        return response;
    }
}
