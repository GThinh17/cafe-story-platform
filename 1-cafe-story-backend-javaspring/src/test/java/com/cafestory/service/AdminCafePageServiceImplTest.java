package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminCafePageStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.mapper.CafePageMapper;
import com.cafestory.repository.CafePageRatingRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.service.serviceImplement.AdminCafePageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminCafePageServiceImpl} — bảng quản trị trang quán.
 *
 * <p>Chú ý phần điểm đánh giá: danh sách lấy theo lô bằng một truy vấn gộp, còn
 * chi tiết lấy riêng từng trang; cả hai nhánh "chưa có đánh giá nào" đều phải trả
 * điểm null và số lượt bằng 0 thay vì ném lỗi.
 */
@ExtendWith(MockitoExtension.class)
class AdminCafePageServiceImplTest {

    @Mock
    private CafePageRepository cafePageRepository;
    @Mock
    private CafePageMapper cafePageMapper;
    @Mock
    private CafePageRatingRepository cafePageRatingRepository;

    private AdminCafePageServiceImpl adminCafePageService;

    private CafePage cafePage;

    @BeforeEach
    void setUp() {
        adminCafePageService = new AdminCafePageServiceImpl(
                cafePageRepository, cafePageMapper, cafePageRatingRepository);
        cafePage = cafePage();
    }

    @Test
    void getCafePages_success_attachesRatingSummary_TC001() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(cafePageRepository.findAdminCafePages(PageStatus.ACTIVE, null, pageable))
                .thenReturn(new PageImpl<>(List.of(cafePage)));
        when(cafePageRatingRepository.summarizeByCafePageIds(List.of(cafePage.getId())))
                .thenReturn(List.of(summaryRow(cafePage.getId(), 4.5, 12L)));
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(new CafePageResponseDTO());

        Page<CafePageResponseDTO> result =
                adminCafePageService.getCafePages(PageStatus.ACTIVE, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        CafePageResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getRatingScore()).isEqualTo(4.5);
        assertThat(dto.getRatingCount()).isEqualTo(12L);
        assertThat(dto.getIsFollowing()).isFalse();
        assertThat(dto.getIsLiked()).isFalse();
        assertThat(dto.getIsRating()).isFalse();
        assertThat(dto.getMyRating()).isNull();
    }

    @Test
    void getCafePages_success_pageWithoutRatingRow_TC002() {
        PageRequest pageable = PageRequest.of(0, 10);
        UUID ownerId = UUID.randomUUID();
        when(cafePageRepository.findAdminCafePages(null, ownerId, pageable))
                .thenReturn(new PageImpl<>(List.of(cafePage)));
        when(cafePageRatingRepository.summarizeByCafePageIds(List.of(cafePage.getId())))
                .thenReturn(List.of());
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(new CafePageResponseDTO());

        CafePageResponseDTO dto = adminCafePageService.getCafePages(null, ownerId, pageable)
                .getContent()
                .get(0);

        assertThat(dto.getRatingScore()).isNull();
        assertThat(dto.getRatingCount()).isZero();
    }

    @Test
    void getCafePages_success_emptyPageSkipsRatingQuery_TC003() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(cafePageRepository.findAdminCafePages(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(adminCafePageService.getCafePages(null, null, pageable).getContent()).isEmpty();
        verify(cafePageRatingRepository, never()).summarizeByCafePageIds(any());
    }

    @Test
    void getCafePage_success_loadsRatingPerPage_TC004() {
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(new CafePageResponseDTO());
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePage.getId())).thenReturn(3.75);
        when(cafePageRatingRepository.countByCafePageId(cafePage.getId())).thenReturn(4L);

        CafePageResponseDTO result = adminCafePageService.getCafePage(cafePage.getId());

        assertThat(result.getRatingScore()).isEqualTo(3.75);
        assertThat(result.getRatingCount()).isEqualTo(4L);
    }

    @Test
    void getCafePage_fail_notFound_TC005() {
        UUID pageId = UUID.randomUUID();
        when(cafePageRepository.findById(pageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCafePageService.getCafePage(pageId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cafe page not found");
    }

    @Test
    void updateCafePageStatus_success_activeAlsoTurnsPageActiveOn_TC006() {
        AdminCafePageStatusUpdateRequestDTO request = new AdminCafePageStatusUpdateRequestDTO();
        request.setStatus(PageStatus.ACTIVE);
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(cafePageRepository.save(cafePage)).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(new CafePageResponseDTO());
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePage.getId())).thenReturn(null);
        when(cafePageRatingRepository.countByCafePageId(cafePage.getId())).thenReturn(0L);

        adminCafePageService.updateCafePageStatus(cafePage.getId(), request);

        assertThat(cafePage.getStatus()).isEqualTo(PageStatus.ACTIVE);
        assertThat(cafePage.getPageActive()).isTrue();
    }

    @Test
    void updateCafePageStatus_success_suspendedTurnsPageActiveOff_TC007() {
        AdminCafePageStatusUpdateRequestDTO request = new AdminCafePageStatusUpdateRequestDTO();
        request.setStatus(PageStatus.SUSPENDED);
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(cafePageRepository.save(cafePage)).thenReturn(cafePage);
        when(cafePageMapper.toCafePageResponseDTO(cafePage)).thenReturn(new CafePageResponseDTO());
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePage.getId())).thenReturn(2.0);
        when(cafePageRatingRepository.countByCafePageId(cafePage.getId())).thenReturn(1L);

        adminCafePageService.updateCafePageStatus(cafePage.getId(), request);

        assertThat(cafePage.getStatus()).isEqualTo(PageStatus.SUSPENDED);
        assertThat(cafePage.getPageActive()).isFalse();
    }

    @Test
    void deleteCafePage_success_removesPage_TC008() {
        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));

        adminCafePageService.deleteCafePage(cafePage.getId());

        verify(cafePageRepository).delete(cafePage);
    }

    @Test
    void deleteCafePage_fail_notFound_TC009() {
        UUID pageId = UUID.randomUUID();
        when(cafePageRepository.findById(pageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCafePageService.deleteCafePage(pageId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cafe page not found");
        verify(cafePageRepository, never()).delete(any(CafePage.class));
    }

    private CafePage cafePage() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());
        owner.setUserName("owner");
        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setOwner(owner);
        page.setName("Cafe Story Ninh Kieu");
        page.setStatus(PageStatus.DRAFT);
        page.setPageActive(false);
        return page;
    }

    private CafePageRatingRepository.CafePageRatingSummaryRow summaryRow(
            UUID cafePageId, Double avgRating, Long ratingCount) {
        return new CafePageRatingRepository.CafePageRatingSummaryRow() {
            @Override
            public UUID getCafePageId() {
                return cafePageId;
            }

            @Override
            public Double getAvgRating() {
                return avgRating;
            }

            @Override
            public Long getRatingCount() {
                return ratingCount;
            }
        };
    }
}
