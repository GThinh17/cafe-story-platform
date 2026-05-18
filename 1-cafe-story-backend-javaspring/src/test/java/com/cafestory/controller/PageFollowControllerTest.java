package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageInteractionRequestDTO;
import com.cafestory.dto.responseDTO.PageFollowResponseDTO;
import com.cafestory.service.serviceInterface.PageFollowService;
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
class PageFollowControllerTest {

    @Mock
    private PageFollowService pageFollowService;

    @InjectMocks
    private PageFollowController pageFollowController;

    @Test
    void followPage_success_TC001() {
        UUID cafePageId = UUID.randomUUID();
        CafePageInteractionRequestDTO request = request();
        PageFollowResponseDTO response = response();

        when(pageFollowService.followPage(cafePageId, request.getUserId())).thenReturn(response);

        PageFollowResponseDTO result = pageFollowController.followPage(cafePageId, request);

        assertThat(result).isEqualTo(response);
        verify(pageFollowService).followPage(cafePageId, request.getUserId());
    }

    @Test
    void unfollowPage_success_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        pageFollowController.unfollowPage(cafePageId, userId);

        verify(pageFollowService).unfollowPage(cafePageId, userId);
    }

    @Test
    void getFollowersByCafePageId_success_TC003() {
        UUID cafePageId = UUID.randomUUID();
        List<PageFollowResponseDTO> response = List.of(response());

        when(pageFollowService.getFollowersByCafePageId(cafePageId)).thenReturn(response);

        List<PageFollowResponseDTO> result = pageFollowController.getFollowersByCafePageId(cafePageId);

        assertThat(result).isEqualTo(response);
        verify(pageFollowService).getFollowersByCafePageId(cafePageId);
    }

    @Test
    void getFollowedPagesByUserId_success_TC004() {
        UUID userId = UUID.randomUUID();
        List<PageFollowResponseDTO> response = List.of(response());

        when(pageFollowService.getFollowedPagesByUserId(userId)).thenReturn(response);

        List<PageFollowResponseDTO> result = pageFollowController.getFollowedPagesByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(pageFollowService).getFollowedPagesByUserId(userId);
    }

    private CafePageInteractionRequestDTO request() {
        CafePageInteractionRequestDTO request = new CafePageInteractionRequestDTO();
        request.setUserId(UUID.randomUUID());
        return request;
    }

    private PageFollowResponseDTO response() {
        PageFollowResponseDTO response = new PageFollowResponseDTO();
        response.setId(UUID.randomUUID());
        response.setCafePageId(UUID.randomUUID());
        response.setUserId(UUID.randomUUID());
        return response;
    }
}
