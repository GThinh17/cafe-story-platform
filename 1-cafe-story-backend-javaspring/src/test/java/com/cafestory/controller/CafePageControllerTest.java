package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.service.serviceInterface.CafePageService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
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
class CafePageControllerTest {

    @Mock
    private CafePageService cafePageService;

    @InjectMocks
    private CafePageController cafePageController;

    @Test
    void createCafePage_success_TC001() {
        UUID userId = UUID.randomUUID();
        CafePageCreateDTO request = createRequest();
        CafePageResponseDTO response = response();

        when(cafePageService.createCafePage(request)).thenReturn(response);

        CafePageResponseDTO result = cafePageController.createCafePage(request, principal(userId));

        assertThat(result).isEqualTo(response);
        assertThat(request.getOwnerUserId()).isEqualTo(userId);
        verify(cafePageService).createCafePage(request);
    }

    @Test
    void getCafePages_success_allPages_TC002() {
        List<CafePageResponseDTO> response = List.of(response());

        when(cafePageService.getAllCafePages()).thenReturn(response);

        List<CafePageResponseDTO> result = cafePageController.getCafePages(null);

        assertThat(result).isEqualTo(response);
        verify(cafePageService).getAllCafePages();
    }

    @Test
    void getCafePages_success_byOwner_TC003() {
        UUID ownerUserId = UUID.randomUUID();
        List<CafePageResponseDTO> response = List.of(response());

        when(cafePageService.getCafePagesByOwnerId(ownerUserId)).thenReturn(response);

        List<CafePageResponseDTO> result = cafePageController.getCafePages(ownerUserId);

        assertThat(result).isEqualTo(response);
        verify(cafePageService).getCafePagesByOwnerId(ownerUserId);
    }

    @Test
    void getCafePageById_success_TC004() {
        UUID cafePageId = UUID.randomUUID();
        CafePageResponseDTO response = response();

        when(cafePageService.getCafePageById(cafePageId)).thenReturn(response);

        CafePageResponseDTO result = cafePageController.getCafePageById(cafePageId);

        assertThat(result).isEqualTo(response);
        verify(cafePageService).getCafePageById(cafePageId);
    }

    @Test
    void getBlogsByCafePageId_success_TC005() {
        UUID cafePageId = UUID.randomUUID();
        List<BlogResponseDTO> response = List.of(new BlogResponseDTO());

        when(cafePageService.getBlogsByCafePageId(cafePageId)).thenReturn(response);

        List<BlogResponseDTO> result = cafePageController.getBlogsByCafePageId(cafePageId);

        assertThat(result).isEqualTo(response);
        verify(cafePageService).getBlogsByCafePageId(cafePageId);
    }

    @Test
    void updateCafePage_success_TC006() {
        UUID userId = UUID.randomUUID();
        UUID cafePageId = UUID.randomUUID();
        CafePageUpdateDTO request = new CafePageUpdateDTO();
        CafePageResponseDTO response = response();

        when(cafePageService.updateCafePage(cafePageId, userId, request)).thenReturn(response);

        CafePageResponseDTO result = cafePageController.updateCafePage(cafePageId, request, principal(userId));

        assertThat(result).isEqualTo(response);
        verify(cafePageService).updateCafePage(cafePageId, userId, request);
    }

    @Test
    void deleteCafePage_success_TC007() {
        UUID userId = UUID.randomUUID();
        UUID cafePageId = UUID.randomUUID();

        cafePageController.deleteCafePage(cafePageId, principal(userId));

        verify(cafePageService).deleteCafePage(cafePageId, userId);
    }

    private CafePageCreateDTO createRequest() {
        CafePageCreateDTO request = new CafePageCreateDTO();
        request.setName("Cafe Story");
        request.setAddress("123 Nguyen Hue");
        return request;
    }

    private CafePageResponseDTO response() {
        CafePageResponseDTO response = new CafePageResponseDTO();
        response.setId(UUID.randomUUID());
        response.setOwnerUserId(UUID.randomUUID());
        response.setName("Cafe Story");
        response.setAddress("123 Nguyen Hue");
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
