package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdFeeRequestDTO;
import com.cafestory.dto.responseDTO.AdFeeResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.entity.enums.AdFeeType;
import com.cafestory.repository.AdFeeRepository;
import com.cafestory.service.serviceImplement.AdFeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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
class AdFeeServiceImplTest {

    private final UUID adFeeId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AdFeeRepository adFeeRepository;

    private AdFeeServiceImpl adFeeService;

    @BeforeEach
    void setUp() {
        adFeeService = new AdFeeServiceImpl(adFeeRepository);
    }

    @Test
    void createAdFee_success_defaultsCurrencyAndStatus_TC001() {
        AdFeeRequestDTO request = request();
        request.setCurrency(null);
        request.setStatus(null);
        mockSave();

        AdFeeResponseDTO result = adFeeService.createAdFee(request);

        assertThat(result.getAdFeeId()).isEqualTo(adFeeId);
        assertThat(result.getFeeType()).isEqualTo(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(500000));
        assertThat(result.getCurrency()).isEqualTo("VND");
        assertThat(result.getStatus()).isTrue();
    }

    @Test
    void getAllAdFees_success_orderedByCreatedAtDesc_TC002() {
        AdFee adFee = adFee();
        when(adFeeRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(adFee));

        List<AdFeeResponseDTO> result = adFeeService.getAllAdFees();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAdFeeId()).isEqualTo(adFeeId);
        verify(adFeeRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void deleteAdFee_success_TC003() {
        AdFee adFee = adFee();
        when(adFeeRepository.findById(adFeeId)).thenReturn(Optional.of(adFee));

        adFeeService.deleteAdFee(adFeeId);

        verify(adFeeRepository).delete(adFee);
    }

    @Test
    void deleteAdFee_fail_notFound_TC004() {
        when(adFeeRepository.findById(adFeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adFeeService.deleteAdFee(adFeeId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private AdFeeRequestDTO request() {
        AdFeeRequestDTO request = new AdFeeRequestDTO();
        request.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        request.setPrice(BigDecimal.valueOf(500000));
        request.setCurrency("VND");
        request.setStatus(true);
        return request;
    }

    private AdFee adFee() {
        AdFee adFee = new AdFee();
        adFee.setAdFeeId(adFeeId);
        adFee.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        adFee.setPrice(BigDecimal.valueOf(500000));
        adFee.setCurrency("VND");
        adFee.setStatus(true);
        adFee.setCreatedAt(LocalDateTime.now());
        return adFee;
    }

    private void mockSave() {
        doAnswer(invocation -> {
            AdFee adFee = invocation.getArgument(0);
            adFee.setAdFeeId(adFeeId);
            adFee.setCreatedAt(LocalDateTime.now());
            return adFee;
        }).when(adFeeRepository).save(any(AdFee.class));
    }
}
