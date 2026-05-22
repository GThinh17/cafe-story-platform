package com.cafestory.controller;

import com.cafestory.config.GlobalResponseAdvice;
import com.cafestory.dto.requestDTO.CreatePaymentRequestDTO;
import com.cafestory.dto.responseDTO.PaymentResponseDTO;
import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.dto.responseDTO.VnpayReturnResponseDTO;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceInterface.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalResponseAdvice(objectMapper))
                .build();
    }

    @Test
    void createPayment_success_stripeCard_TC001() {
        CreatePaymentRequestDTO request = request(PaymentMethod.STRIPE_CARD);
        PaymentResponseDTO response = response(PaymentMethod.STRIPE_CARD);
        response.setPaymentUrl("https://checkout.stripe.com/test");
        when(paymentService.createPayment(request)).thenReturn(response);

        PaymentResponseDTO result = paymentController.createPayment(request);

        assertThat(result.getPaymentUrl()).isEqualTo("https://checkout.stripe.com/test");
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.STRIPE_CARD);
        verify(paymentService).createPayment(request);
    }

    @Test
    void createPayment_success_bankTransfer_TC002() {
        CreatePaymentRequestDTO request = request(PaymentMethod.BANK_TRANSFER);
        PaymentResponseDTO response = response(PaymentMethod.BANK_TRANSFER);
        response.setTransferContent("CAFE_PAYMENT_123");
        when(paymentService.createPayment(request)).thenReturn(response);

        PaymentResponseDTO result = paymentController.createPayment(request);

        assertThat(result.getTransferContent()).isEqualTo("CAFE_PAYMENT_123");
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        verify(paymentService).createPayment(request);
    }

    @Test
    void createPayment_success_vnpay_TC006() {
        CreatePaymentRequestDTO request = request(PaymentMethod.VNPAY);
        PaymentResponseDTO response = response(PaymentMethod.VNPAY);
        response.setPaymentUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=123&vnp_SecureHash=abc");
        when(paymentService.createPayment(request)).thenReturn(response);

        PaymentResponseDTO result = paymentController.createPayment(request);

        assertThat(result.getPaymentUrl()).contains("vnp_SecureHash=abc");
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.VNPAY);
        verify(paymentService).createPayment(request);
    }

    @Test
    void getPayment_success_TC003() {
        UUID paymentId = UUID.randomUUID();
        PaymentResponseDTO response = response(PaymentMethod.STRIPE_CARD);
        when(paymentService.getPayment(paymentId)).thenReturn(response);

        PaymentResponseDTO result = paymentController.getPayment(paymentId);

        assertThat(result).isEqualTo(response);
        verify(paymentService).getPayment(paymentId);
    }

    @Test
    void getAllPayments_success_withoutStatusFilter_TC009() {
        List<PaymentResponseDTO> response = List.of(response(PaymentMethod.VNPAY));
        when(paymentService.getAllPayments(null)).thenReturn(response);

        List<PaymentResponseDTO> result = paymentController.getAllPayments(null);

        assertThat(result).isEqualTo(response);
        verify(paymentService).getAllPayments(null);
    }

    @Test
    void getAllPayments_success_withStatusFilter_TC010() throws Exception {
        PaymentResponseDTO response = response(PaymentMethod.VNPAY);
        response.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentService.getAllPayments(PaymentStatus.PENDING)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payments")
                        .param("paymentStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].paymentStatus").value("PENDING"));

        verify(paymentService).getAllPayments(PaymentStatus.PENDING);
    }

    @Test
    void markBankTransferPaid_success_TC004() {
        UUID paymentId = UUID.randomUUID();
        PaymentResponseDTO response = response(PaymentMethod.BANK_TRANSFER);
        response.setPaymentStatus(PaymentStatus.PAID);
        when(paymentService.markBankTransferPaid(paymentId)).thenReturn(response);

        PaymentResponseDTO result = paymentController.markBankTransferPaid(paymentId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentService).markBankTransferPaid(paymentId);
    }

    @Test
    void createPayment_fail_invalidRequest_TC005() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleVnpayReturn_success_returnsWrappedStatus_TC007() throws Exception {
        VnpayReturnResponseDTO response = new VnpayReturnResponseDTO();
        response.setPaymentId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        response.setStatus("success");
        response.setPaymentStatus(PaymentStatus.PAID);
        response.setResponseCode("00");
        when(paymentService.handleVnpayReturn(anyMap())).thenReturn(response);

        mockMvc.perform(get("/api/payments/vnpay/return")
                        .param("vnp_TxnRef", "33333333-3333-3333-3333-333333333333")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "valid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
    }

    @Test
    void handleVnpayIpn_success_returnsRawVnpayResponse_TC008() throws Exception {
        when(paymentService.handleVnpayIpn(anyMap()))
                .thenReturn(new VnpayIpnResponseDTO("00", "Confirm Success"));

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", "33333333-3333-3333-3333-333333333333")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "valid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"))
                .andExpect(jsonPath("$.statusCode").doesNotExist());
    }

    private CreatePaymentRequestDTO request(PaymentMethod method) {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setBuyerId(UUID.randomUUID());
        request.setExtraFeeId(UUID.randomUUID());
        request.setPaymentMethod(method);
        return request;
    }

    private PaymentResponseDTO response(PaymentMethod method) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(UUID.randomUUID());
        response.setBuyerId(UUID.randomUUID());
        response.setExtraFeeId(UUID.randomUUID());
        response.setPaymentMethod(method);
        response.setAmount(BigDecimal.valueOf(299000));
        response.setCurrency("VND");
        response.setPaymentStatus(PaymentStatus.PENDING);
        return response;
    }
}
