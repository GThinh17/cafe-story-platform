package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminExtraFeeStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.repository.ExtraFeeRepository;
import com.cafestory.service.serviceImplement.AdminExtraFeeServiceImpl;
import com.cafestory.service.serviceInterface.ExtraFeeService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminExtraFeeServiceImpl}.
 *
 * <p>Điểm nghiệp vụ duy nhất của lớp này là {@code maxMembers}: gói mở trang quán
 * không khai báo số thành viên thì mặc định là 2, các loại phí khác giữ nguyên
 * giá trị trong cơ sở dữ liệu.
 */
@ExtendWith(MockitoExtension.class)
class AdminExtraFeeServiceImplTest {

    @Mock
    private ExtraFeeRepository extraFeeRepository;
    @Mock
    private ExtraFeeService extraFeeService;

    private AdminExtraFeeServiceImpl adminExtraFeeService;

    @BeforeEach
    void setUp() {
        adminExtraFeeService = new AdminExtraFeeServiceImpl(extraFeeRepository, extraFeeService);
    }

    @Test
    void getExtraFees_success_nullStatusListsEverything_TC001() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(extraFeeRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(extraFee(ExtraFeeType.CAFE_PAGE_OPENING, null))));

        Page<ExtraFeeResponseDTO> result = adminExtraFeeService.getExtraFees(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMaxMembers()).isEqualTo(2);
    }

    @Test
    void getExtraFees_success_filtersByStatus_TC002() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(extraFeeRepository.findByStatus(true, pageable))
                .thenReturn(new PageImpl<>(List.of(extraFee(ExtraFeeType.CAFE_PAGE_OPENING, 5))));

        ExtraFeeResponseDTO dto = adminExtraFeeService.getExtraFees(true, pageable).getContent().get(0);

        assertThat(dto.getMaxMembers()).isEqualTo(5);
        assertThat(dto.getName()).isEqualTo("Goi mo trang quan");
        assertThat(dto.getPrice()).isEqualTo(199_000L);
        assertThat(dto.getDurationMonths()).isEqualTo(12);
        assertThat(dto.getStatus()).isTrue();
    }

    @Test
    void getExtraFees_success_nonPageOpeningKeepsNullMaxMembers_TC003() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(extraFeeRepository.findByStatus(false, pageable))
                .thenReturn(new PageImpl<>(List.of(extraFee(ExtraFeeType.REVIEWER_REGISTRATION, null))));

        assertThat(adminExtraFeeService.getExtraFees(false, pageable).getContent().get(0).getMaxMembers())
                .isNull();
    }

    @Test
    void createExtraFee_success_delegatesToExtraFeeService_TC004() {
        ExtraFeeRequestDTO request = new ExtraFeeRequestDTO();
        ExtraFeeResponseDTO expected = new ExtraFeeResponseDTO();
        when(extraFeeService.createExtraFee(request)).thenReturn(expected);

        assertThat(adminExtraFeeService.createExtraFee(request)).isSameAs(expected);
    }

    @Test
    void updateExtraFee_success_delegatesToExtraFeeService_TC005() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFeeRequestDTO request = new ExtraFeeRequestDTO();
        ExtraFeeResponseDTO expected = new ExtraFeeResponseDTO();
        when(extraFeeService.updateExtraFee(extraFeeId, request)).thenReturn(expected);

        assertThat(adminExtraFeeService.updateExtraFee(extraFeeId, request)).isSameAs(expected);
    }

    @Test
    void deleteExtraFee_success_delegatesToExtraFeeService_TC006() {
        UUID extraFeeId = UUID.randomUUID();

        adminExtraFeeService.deleteExtraFee(extraFeeId);

        verify(extraFeeService).deleteExtraFee(extraFeeId);
    }

    @Test
    void updateExtraFeeStatus_success_togglesStatus_TC007() {
        ExtraFee extraFee = extraFee(ExtraFeeType.CAFE_PAGE_OPENING, 3);
        AdminExtraFeeStatusUpdateRequestDTO request = new AdminExtraFeeStatusUpdateRequestDTO();
        request.setStatus(false);
        when(extraFeeRepository.findById(extraFee.getExtraFeeId())).thenReturn(Optional.of(extraFee));
        when(extraFeeRepository.save(extraFee)).thenReturn(extraFee);

        ExtraFeeResponseDTO result =
                adminExtraFeeService.updateExtraFeeStatus(extraFee.getExtraFeeId(), request);

        assertThat(result.getStatus()).isFalse();
        assertThat(extraFee.getStatus()).isFalse();
    }

    @Test
    void updateExtraFeeStatus_fail_notFound_TC008() {
        UUID extraFeeId = UUID.randomUUID();
        AdminExtraFeeStatusUpdateRequestDTO request = new AdminExtraFeeStatusUpdateRequestDTO();
        request.setStatus(true);
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminExtraFeeService.updateExtraFeeStatus(extraFeeId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Extra fee not found");
    }

    private ExtraFee extraFee(ExtraFeeType feeType, Integer maxMembers) {
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(UUID.randomUUID());
        extraFee.setName("Goi mo trang quan");
        extraFee.setDescription("Cho phep mo mot trang quan");
        extraFee.setFeeType(feeType);
        extraFee.setPrice(199_000L);
        extraFee.setDurationMonths(12);
        extraFee.setMaxMembers(maxMembers);
        extraFee.setStatus(true);
        extraFee.setCreatedAt(LocalDateTime.now());
        extraFee.setUpdatedAt(LocalDateTime.now());
        return extraFee;
    }
}
