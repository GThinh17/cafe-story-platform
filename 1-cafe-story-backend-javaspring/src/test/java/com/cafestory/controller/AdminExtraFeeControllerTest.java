package com.cafestory.controller;

import com.cafestory.config.GlobalResponseAdvice;
import com.cafestory.dto.requestDTO.AdminExtraFeeStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.service.serviceInterface.AdminExtraFeeService;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AdminExtraFeeControllerTest {

    @Mock
    private AdminExtraFeeService adminExtraFeeService;

    @InjectMocks
    private AdminExtraFeeController adminExtraFeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(adminExtraFeeController)
                .setControllerAdvice(new GlobalResponseAdvice(objectMapper))
                .build();
    }

    @Test
    void createExtraFee_success_TC001() {
        ExtraFeeRequestDTO request = request();
        ExtraFeeResponseDTO response = response();
        when(adminExtraFeeService.createExtraFee(request)).thenReturn(response);

        ExtraFeeResponseDTO result = adminExtraFeeController.createExtraFee(request);

        assertThat(result).isEqualTo(response);
        verify(adminExtraFeeService).createExtraFee(request);
    }

    @Test
    void createExtraFee_fail_invalidRequest_TC002() throws Exception {
        mockMvc.perform(post("/api/admin/extra-fees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExtraFeeStatus_fail_notFound_TC003() throws Exception {
        UUID extraFeeId = UUID.randomUUID();
        AdminExtraFeeStatusUpdateRequestDTO request = new AdminExtraFeeStatusUpdateRequestDTO();
        request.setStatus(false);
        when(adminExtraFeeService.updateExtraFeeStatus(extraFeeId, request))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found"));

        mockMvc.perform(patch("/api/admin/extra-fees/{extraFeeId}/status", extraFeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExtraFees_success_normalizesPaging_TC004() {
        org.springframework.data.domain.Page<ExtraFeeResponseDTO> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(response()));
        when(adminExtraFeeService.getExtraFees(
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        assertThat(adminExtraFeeController.getExtraFees(true, -1, 400)).isSameAs(page);

        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(adminExtraFeeService).getExtraFees(
                org.mockito.ArgumentMatchers.eq(true), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    void updateExtraFee_success_delegates_TC005() {
        UUID extraFeeId = UUID.randomUUID();
        ExtraFeeRequestDTO request = request();
        ExtraFeeResponseDTO response = response();
        when(adminExtraFeeService.updateExtraFee(extraFeeId, request)).thenReturn(response);

        assertThat(adminExtraFeeController.updateExtraFee(extraFeeId, request)).isSameAs(response);
    }

    @Test
    void updateExtraFeeStatus_success_delegates_TC006() {
        UUID extraFeeId = UUID.randomUUID();
        AdminExtraFeeStatusUpdateRequestDTO request = new AdminExtraFeeStatusUpdateRequestDTO();
        request.setStatus(false);
        ExtraFeeResponseDTO response = response();
        when(adminExtraFeeService.updateExtraFeeStatus(extraFeeId, request)).thenReturn(response);

        assertThat(adminExtraFeeController.updateExtraFeeStatus(extraFeeId, request)).isSameAs(response);
    }

    @Test
    void deleteExtraFee_success_delegates_TC007() {
        UUID extraFeeId = UUID.randomUUID();

        adminExtraFeeController.deleteExtraFee(extraFeeId);

        verify(adminExtraFeeService).deleteExtraFee(extraFeeId);
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
