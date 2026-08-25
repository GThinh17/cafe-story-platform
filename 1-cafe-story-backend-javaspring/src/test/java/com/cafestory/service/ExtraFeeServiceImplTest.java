package com.cafestory.service;

import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.repository.ExtraFeeRepository;
import com.cafestory.service.serviceImplement.ExtraFeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtraFeeServiceImplTest {

    @Mock
    private ExtraFeeRepository extraFeeRepository;

    @InjectMocks
    private ExtraFeeServiceImpl extraFeeService;

    @Test
    void createExtraFee_success_TC001() {
        ExtraFeeRequestDTO request = request();
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.createExtraFee(request);

        assertThat(result.getExtraFeeId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Reviewer monthly package");
        assertThat(result.getDescription()).isEqualTo("Monthly fee to become reviewer");
        assertThat(result.getFeeType()).isEqualTo(ExtraFeeType.REVIEWER_REGISTRATION);
        assertThat(result.getPrice()).isEqualTo(99000);
        assertThat(result.getDurationMonths()).isEqualTo(1);
        assertThat(result.getMaxMembers()).isNull();
        assertThat(result.getStatus()).isFalse();
        verify(extraFeeRepository).save(any(ExtraFee.class));
    }

    @Test
    void createExtraFee_success_defaultStatusTrueWhenNull_TC002() {
        ExtraFeeRequestDTO request = request();
        request.setStatus(null);
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.createExtraFee(request);

        assertThat(result.getStatus()).isTrue();
    }

    @Test
    void updateExtraFee_success_TC003() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFee extraFee = extraFee(extraFeeId);
        ExtraFeeRequestDTO request = request();
        request.setName("Cafe page opening package");
        request.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        request.setPrice(199000L);
        request.setDurationMonths(3);
        request.setStatus(false);

        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.updateExtraFee(extraFeeId, request);

        assertThat(result.getExtraFeeId()).isEqualTo(extraFeeId);
        assertThat(result.getName()).isEqualTo("Cafe page opening package");
        assertThat(result.getFeeType()).isEqualTo(ExtraFeeType.CAFE_PAGE_OPENING);
        assertThat(result.getPrice()).isEqualTo(199000);
        assertThat(result.getDurationMonths()).isEqualTo(3);
        assertThat(result.getStatus()).isFalse();
        assertThat(extraFee.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 0));
    }

    @Test
    void updateExtraFee_success_keepsExistingStatusWhenNull_TC004() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFee extraFee = extraFee(extraFeeId);
        extraFee.setStatus(false);
        ExtraFeeRequestDTO request = request();
        request.setStatus(null);

        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.updateExtraFee(extraFeeId, request);

        assertThat(result.getStatus()).isFalse();
    }

    @Test
    void updateExtraFee_fail_notFound_TC005() {
        UUID extraFeeId = UUID.randomUUID();
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> extraFeeService.updateExtraFee(extraFeeId, request()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createExtraFee_success_cafePageOpeningDefaultsMaxMembers_TC007() {
        ExtraFeeRequestDTO request = request();
        request.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        request.setMaxMembers(null);
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.createExtraFee(request);

        assertThat(result.getMaxMembers()).isEqualTo(2);
    }

    @Test
    void createExtraFee_success_cafePageOpeningUsesRequestedMaxMembers_TC008() {
        ExtraFeeRequestDTO request = request();
        request.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        request.setMaxMembers(5);
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.createExtraFee(request);

        assertThat(result.getMaxMembers()).isEqualTo(5);
    }

    @Test
    void createExtraFee_success_reviewerMaxMembersStaysNull_TC009() {
        ExtraFeeRequestDTO request = request();
        request.setFeeType(ExtraFeeType.REVIEWER_REGISTRATION);
        request.setMaxMembers(null);
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.createExtraFee(request);

        assertThat(result.getMaxMembers()).isNull();
    }

    @Test
    void createExtraFee_fail_maxMembersLessThanOne_TC010() {
        ExtraFeeRequestDTO request = request();
        request.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        request.setMaxMembers(0);

        assertThatThrownBy(() -> extraFeeService.createExtraFee(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateExtraFee_success_cafePageOpeningDefaultsMaxMembersWhenNull_TC011() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFee extraFee = extraFee(extraFeeId);
        ExtraFeeRequestDTO request = request();
        request.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        request.setMaxMembers(null);
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.updateExtraFee(extraFeeId, request);

        assertThat(result.getMaxMembers()).isEqualTo(2);
    }

    @Test
    void updateExtraFee_success_usesRequestedMaxMembers_TC012() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFee extraFee = extraFee(extraFeeId);
        ExtraFeeRequestDTO request = request();
        request.setFeeType(ExtraFeeType.CAFE_PAGE_OPENING);
        request.setMaxMembers(7);
        when(extraFeeRepository.findById(extraFeeId)).thenReturn(Optional.of(extraFee));
        mockSave();

        ExtraFeeResponseDTO result = extraFeeService.updateExtraFee(extraFeeId, request);

        assertThat(result.getMaxMembers()).isEqualTo(7);
    }

    @Test
    void deleteExtraFee_success_TC013() {
        UUID extraFeeId = UUID.randomUUID();
        when(extraFeeRepository.existsById(extraFeeId)).thenReturn(true);

        extraFeeService.deleteExtraFee(extraFeeId);

        verify(extraFeeRepository).deleteById(extraFeeId);
    }

    @Test
    void deleteExtraFee_fail_notFound_TC014() {
        UUID extraFeeId = UUID.randomUUID();
        when(extraFeeRepository.existsById(extraFeeId)).thenReturn(false);

        assertThatThrownBy(() -> extraFeeService.deleteExtraFee(extraFeeId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getAllExtraFees_success_TC006() {
        ExtraFee extraFee = extraFee(UUID.randomUUID());
        when(extraFeeRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(extraFee));

        List<ExtraFeeResponseDTO> result = extraFeeService.getAllExtraFees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExtraFeeId()).isEqualTo(extraFee.getExtraFeeId());
        assertThat(result.get(0).getFeeType()).isEqualTo(ExtraFeeType.REVIEWER_REGISTRATION);
    }

    // @Test
    // void serviceDependencies_success_noRequesterAdminRoleDependency_TC007() {
    // assertThat(ExtraFeeServiceImpl.class.getDeclaredFields())
    // .extracting(Field::getType)
    // .containsExactly(ExtraFeeRepository.class);
    // }

    private ExtraFeeRequestDTO request() {
        ExtraFeeRequestDTO request = new ExtraFeeRequestDTO();
        request.setName("Reviewer monthly package");
        request.setDescription("Monthly fee to become reviewer");
        request.setFeeType(ExtraFeeType.REVIEWER_REGISTRATION);
        request.setPrice(99000L);
        request.setDurationMonths(1);
        request.setMaxMembers(null);
        request.setStatus(false);
        return request;
    }

    private ExtraFee extraFee(UUID extraFeeId) {
        ExtraFee extraFee = new ExtraFee();
        extraFee.setExtraFeeId(extraFeeId);
        extraFee.setName("Reviewer monthly package");
        extraFee.setDescription("Monthly fee to become reviewer");
        extraFee.setFeeType(ExtraFeeType.REVIEWER_REGISTRATION);
        extraFee.setPrice(99000);
        extraFee.setDurationMonths(1);
        extraFee.setMaxMembers(null);
        extraFee.setStatus(true);
        extraFee.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        extraFee.setUpdatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        return extraFee;
    }

    private void mockSave() {
        doAnswer(invocation -> {
            ExtraFee extraFee = invocation.getArgument(0);
            if (extraFee.getExtraFeeId() == null) {
                extraFee.setExtraFeeId(UUID.randomUUID());
            }
            if (extraFee.getCreatedAt() == null) {
                extraFee.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
            }
            extraFee.setUpdatedAt(LocalDateTime.of(2026, 5, 1, 10, 30));
            return extraFee;
        }).when(extraFeeRepository).save(any(ExtraFee.class));
    }
}
