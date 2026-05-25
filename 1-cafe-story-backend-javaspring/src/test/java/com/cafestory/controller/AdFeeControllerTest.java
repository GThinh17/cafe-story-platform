package com.cafestory.controller;

import com.cafestory.config.GlobalResponseAdvice;
import com.cafestory.dto.requestDTO.AdFeeRequestDTO;
import com.cafestory.dto.responseDTO.AdFeeResponseDTO;
import com.cafestory.entity.enums.AdFeeType;
import com.cafestory.service.serviceInterface.AdFeeService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AdFeeControllerTest {

    @Mock
    private AdFeeService adFeeService;

    @InjectMocks
    private AdFeeController adFeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(adFeeController)
                .setControllerAdvice(new GlobalResponseAdvice(objectMapper))
                .build();
    }

    @Test
    void createAdFee_success_TC001() {
        AdFeeRequestDTO request = request();
        AdFeeResponseDTO response = response();
        when(adFeeService.createAdFee(request)).thenReturn(response);

        AdFeeResponseDTO result = adFeeController.createAdFee(request);

        assertThat(result).isEqualTo(response);
        verify(adFeeService).createAdFee(request);
    }

    @Test
    void getAllAdFees_success_TC002() {
        List<AdFeeResponseDTO> response = List.of(response());
        when(adFeeService.getAllAdFees()).thenReturn(response);

        List<AdFeeResponseDTO> result = adFeeController.getAllAdFees();

        assertThat(result).isEqualTo(response);
        verify(adFeeService).getAllAdFees();
    }

    @Test
    void deleteAdFee_success_TC003() {
        UUID adFeeId = UUID.randomUUID();

        adFeeController.deleteAdFee(adFeeId);

        verify(adFeeService).deleteAdFee(adFeeId);
    }

    @Test
    void createAdFee_fail_invalidRequestBody_TC004() throws Exception {
        mockMvc.perform(post("/api/ad-fees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAdFee_fail_notFound_TC005() throws Exception {
        UUID adFeeId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad fee not found"))
                .when(adFeeService).deleteAdFee(adFeeId);

        mockMvc.perform(delete("/api/ad-fees/{adFeeId}", adFeeId))
                .andExpect(status().isNotFound());
    }

    private AdFeeRequestDTO request() {
        AdFeeRequestDTO request = new AdFeeRequestDTO();
        request.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        request.setPrice(BigDecimal.valueOf(500000));
        request.setCurrency("VND");
        request.setStatus(true);
        return request;
    }

    private AdFeeResponseDTO response() {
        AdFeeResponseDTO response = new AdFeeResponseDTO();
        response.setAdFeeId(UUID.randomUUID());
        response.setFeeType(AdFeeType.FEED_10000_IMPRESSIONS_OR_30_DAYS);
        response.setPrice(BigDecimal.valueOf(500000));
        response.setCurrency("VND");
        response.setStatus(true);
        return response;
    }
}
