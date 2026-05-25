package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.service.serviceInterface.ExtraFeeService;
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
class ExtraFeeControllerTest {

    @Mock
    private ExtraFeeService extraFeeService;

    @InjectMocks
    private ExtraFeeController extraFeeController;

    @Test
    void getActiveExtraFees_success_TC001() {
        List<ExtraFeeResponseDTO> response = List.of(response());
        when(extraFeeService.getActiveExtraFees()).thenReturn(response);

        List<ExtraFeeResponseDTO> result = extraFeeController.getActiveExtraFees();

        assertThat(result).isEqualTo(response);
        verify(extraFeeService).getActiveExtraFees();
    }

    private ExtraFeeResponseDTO response() {
        ExtraFeeResponseDTO response = new ExtraFeeResponseDTO();
        response.setExtraFeeId(UUID.randomUUID());
        response.setName("Reviewer monthly package");
        response.setDescription("Monthly fee to become reviewer");
        response.setFeeType(ExtraFeeType.REVIEWER_REGISTRATION);
        response.setPrice(99000L);
        response.setDurationMonths(1);
        response.setStatus(true);
        return response;
    }
}
