package com.cafestory.controller;

import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceInterface.AdminPaymentService;
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
 * Kiểm thử {@link AdminPaymentController} — uỷ quyền và chuẩn hoá phân trang.
 */
@ExtendWith(MockitoExtension.class)
class AdminPaymentControllerTest {

    @Mock
    private AdminPaymentService adminPaymentService;

    @InjectMocks
    private AdminPaymentController adminPaymentController;

    @Test
    void getPayments_success_normalizesPaging_TC001() {
        UUID buyerId = UUID.randomUUID();
        Page<PaymentResponseDTO> page = new PageImpl<>(List.of());
        when(adminPaymentService.getPayments(eq(PaymentStatus.PAID), eq(buyerId), any(Pageable.class)))
                .thenReturn(page);

        assertThat(adminPaymentController.getPayments(PaymentStatus.PAID, buyerId, -3, 300)).isSameAs(page);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminPaymentService).getPayments(any(), any(), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getPayment_success_delegates_TC002() {
        UUID paymentId = UUID.randomUUID();
        PaymentResponseDTO response = new PaymentResponseDTO();
        when(adminPaymentService.getPayment(paymentId)).thenReturn(response);

        assertThat(adminPaymentController.getPayment(paymentId)).isSameAs(response);
    }

    @Test
    void markBankTransferPaid_success_delegates_TC003() {
        UUID paymentId = UUID.randomUUID();
        PaymentResponseDTO response = new PaymentResponseDTO();
        when(adminPaymentService.markBankTransferPaid(paymentId)).thenReturn(response);

        assertThat(adminPaymentController.markBankTransferPaid(paymentId)).isSameAs(response);
    }

    @Test
    void refundPayment_success_delegates_TC004() {
        UUID paymentId = UUID.randomUUID();
        PaymentResponseDTO response = new PaymentResponseDTO();
        when(adminPaymentService.refundPayment(paymentId)).thenReturn(response);

        assertThat(adminPaymentController.refundPayment(paymentId)).isSameAs(response);
    }

    @Test
    void expireStalePayments_success_returnsProcessedCount_TC005() {
        when(adminPaymentService.expireStalePayments()).thenReturn(3);

        assertThat(adminPaymentController.expireStalePayments()).isEqualTo(3);
    }
}
