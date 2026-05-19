package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageInteractionRequestDTO;
import com.cafestory.dto.responseDTO.PageLikeResponseDTO;
import com.cafestory.service.serviceInterface.PageLikeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageLikeControllerTest {

    @Mock
    private PageLikeService pageLikeService;

    @InjectMocks
    private PageLikeController pageLikeController;

    @Test
    void likePage_success_TC001() {
        UUID cafePageId = UUID.randomUUID();
        CafePageInteractionRequestDTO request = request();
        PageLikeResponseDTO response = response();

        when(pageLikeService.likePage(cafePageId, request.getUserId())).thenReturn(response);

        PageLikeResponseDTO result = pageLikeController.likePage(cafePageId, request);

        assertThat(result).isEqualTo(response);
        verify(pageLikeService).likePage(cafePageId, request.getUserId());
    }

    @Test
    void unlikePage_success_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        pageLikeController.unlikePage(cafePageId, userId);

        verify(pageLikeService).unlikePage(cafePageId, userId);
    }

    @Test
    void getLikesByCafePageId_success_TC003() {
        UUID cafePageId = UUID.randomUUID();
        List<PageLikeResponseDTO> response = List.of(response());

        when(pageLikeService.getLikesByCafePageId(cafePageId)).thenReturn(response);

        List<PageLikeResponseDTO> result = pageLikeController.getLikesByCafePageId(cafePageId);

        assertThat(result).isEqualTo(response);
        verify(pageLikeService).getLikesByCafePageId(cafePageId);
    }

    @Test
    void getLikesByUserId_success_TC004() {
        UUID userId = UUID.randomUUID();
        List<PageLikeResponseDTO> response = List.of(response());

        when(pageLikeService.getLikesByUserId(userId)).thenReturn(response);

        List<PageLikeResponseDTO> result = pageLikeController.getLikesByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(pageLikeService).getLikesByUserId(userId);
    }

    private CafePageInteractionRequestDTO request() {
        CafePageInteractionRequestDTO request = new CafePageInteractionRequestDTO();
        request.setUserId(UUID.randomUUID());
        return request;
    }

    private PageLikeResponseDTO response() {
        PageLikeResponseDTO response = new PageLikeResponseDTO();
        response.setId(UUID.randomUUID());
        response.setCafePageId(UUID.randomUUID());
        response.setUserId(UUID.randomUUID());
        return response;
    }
}
