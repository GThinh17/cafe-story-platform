package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminCafePageStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminCafePageController} — uỷ quyền và chuẩn hoá phân trang.
 */
@ExtendWith(MockitoExtension.class)
class AdminCafePageControllerTest {

    @Mock
    private AdminCafePageService adminCafePageService;

    @InjectMocks
    private AdminCafePageController adminCafePageController;

    @Test
    void getCafePages_success_normalizesPaging_TC001() {
        UUID ownerUserId = UUID.randomUUID();
        Page<CafePageResponseDTO> page = new PageImpl<>(List.of());
        when(adminCafePageService.getCafePages(eq(PageStatus.ACTIVE), eq(ownerUserId), any(Pageable.class)))
                .thenReturn(page);

        assertThat(adminCafePageController.getCafePages(PageStatus.ACTIVE, ownerUserId, -1, 1_000))
                .isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminCafePageService).getCafePages(any(), any(), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getCafePage_success_delegates_TC002() {
        UUID pageId = UUID.randomUUID();
        CafePageResponseDTO response = new CafePageResponseDTO();
        when(adminCafePageService.getCafePage(pageId)).thenReturn(response);

        assertThat(adminCafePageController.getCafePage(pageId)).isSameAs(response);
    }

    @Test
    void updateCafePageStatus_success_delegates_TC003() {
        UUID pageId = UUID.randomUUID();
        AdminCafePageStatusUpdateRequestDTO request = new AdminCafePageStatusUpdateRequestDTO();
        request.setStatus(PageStatus.SUSPENDED);
        CafePageResponseDTO response = new CafePageResponseDTO();
        when(adminCafePageService.updateCafePageStatus(pageId, request)).thenReturn(response);

        assertThat(adminCafePageController.updateCafePageStatus(pageId, request)).isSameAs(response);
    }

    @Test
    void deleteCafePage_success_delegates_TC004() {
        UUID pageId = UUID.randomUUID();

        adminCafePageController.deleteCafePage(pageId);

        verify(adminCafePageService).deleteCafePage(pageId);
    }
}
