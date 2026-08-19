package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentTranslationRequestDTO;
import com.cafestory.dto.responseDTO.AdminContentTranslationResponseDTO;
import com.cafestory.service.serviceInterface.AdminContentTranslationService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminContentTranslationControllerTest {

    @Test
    void translate_validRequest_delegatesAndPreservesResponseShape() {
        AdminContentTranslationService service = mock(AdminContentTranslationService.class);
        AdminContentTranslationController controller = new AdminContentTranslationController(service);
        AdminContentTranslationRequestDTO request = request();
        AdminContentTranslationResponseDTO response = new AdminContentTranslationResponseDTO();
        response.setRequestId(UUID.randomUUID());
        response.setDetectedLocale("en");
        response.setTargetLocale("vi");
        response.setTranslatedText("Xin chào");
        response.setTranslationState("TRANSLATED");
        response.setModelName("gpt-4o-mini");
        when(service.translate(request)).thenReturn(response);

        AdminContentTranslationResponseDTO result = controller.translate(request);

        assertThat(result).isSameAs(response);
        assertThat(result.getTranslationState()).isEqualTo("TRANSLATED");
        verify(service).translate(request);
    }

    private AdminContentTranslationRequestDTO request() {
        AdminContentTranslationRequestDTO request = new AdminContentTranslationRequestDTO();
        request.setText("Hello");
        request.setTargetLocale("vi");
        request.setContentKind("COMMENT_CONTENT");
        return request;
    }
}
