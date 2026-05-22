package com.cafestory.controller;

import com.cafestory.config.GlobalResponseAdvice;
import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.service.serviceInterface.ExtraFeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ExtraFeeControllerTest {

    @Mock
    private ExtraFeeService extraFeeService;

    @InjectMocks
    private ExtraFeeController extraFeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(extraFeeController)
                .setControllerAdvice(new GlobalResponseAdvice(objectMapper))
                .build();
    }

    @Test
    void createExtraFee_success_TC001() {
        ExtraFeeRequestDTO request = request();
        ExtraFeeResponseDTO response = response();
        when(extraFeeService.createExtraFee(request)).thenReturn(response);

        ExtraFeeResponseDTO result = extraFeeController.createExtraFee(request);

        assertThat(result).isEqualTo(response);
        verify(extraFeeService).createExtraFee(request);
    }

    @Test
    void updateExtraFee_success_TC002() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFeeRequestDTO request = request();
        ExtraFeeResponseDTO response = response();
        when(extraFeeService.updateExtraFee(extraFeeId, request)).thenReturn(response);

        ExtraFeeResponseDTO result = extraFeeController.updateExtraFee(extraFeeId, request);

        assertThat(result).isEqualTo(response);
        verify(extraFeeService).updateExtraFee(extraFeeId, request);
    }

    @Test
    void getAllExtraFees_success_TC003() {
        List<ExtraFeeResponseDTO> response = List.of(response());
        when(extraFeeService.getAllExtraFees()).thenReturn(response);

        List<ExtraFeeResponseDTO> result = extraFeeController.getAllExtraFees();

        assertThat(result).isEqualTo(response);
        verify(extraFeeService).getAllExtraFees();
    }

    @Test
    void createExtraFee_fail_invalidRequestBody_TC004() throws Exception {
        mockMvc.perform(post("/api/extra-fees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExtraFee_fail_notFound_TC005() throws Exception {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFeeRequestDTO request = request();
        when(extraFeeService.updateExtraFee(extraFeeId, request))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found"));

        mockMvc.perform(put("/api/extra-fees/{extraFeeId}", extraFeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private ExtraFeeRequestDTO request() {
        ExtraFeeRequestDTO request = new ExtraFeeRequestDTO();
        request.setName("Reviewer monthly package");
        request.setDescription("Monthly fee to become reviewer");
        request.setFeeType(ExtraFeeType.REVIEWER_REGISTRATION);
        request.setPrice(99000L);
        request.setDurationMonths(1);
        request.setStatus(true);
        return request;
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
