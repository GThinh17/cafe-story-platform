package com.cafestory.config;

import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.dto.responseDTO.VnpayReturnResponseDTO;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceInterface.PaymentService;
import com.cafestory.until.security.JwtAuthenticationFilter;
import com.cafestory.until.security.JwtService;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private PaymentService paymentService;

    @Test
    void me_fail_withoutAccessToken_TC001() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_fail_userRoleCannotAccess_TC002() throws Exception {
        String accessToken = jwtService.createAccessToken(user(), List.of("USER"));

        mockMvc.perform(get("/api/admin/test")
                        .cookie(new Cookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUsers_fail_userRoleCannotAccess_TC007() throws Exception {
        String accessToken = jwtService.createAccessToken(user(), List.of("USER"));

        mockMvc.perform(get("/api/admin/users")
                        .cookie(new Cookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, accessToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void stripeWebhook_success_noAuthorizationDoesNotReturn401_TC003() throws Exception {
        mockMvc.perform(post("/api/payments/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void vnpayReturn_success_noAuthorizationDoesNotReturn401_TC004() throws Exception {
        VnpayReturnResponseDTO response = new VnpayReturnResponseDTO();
        response.setStatus("failed");
        response.setPaymentStatus(PaymentStatus.PENDING);
        response.setMessage("Invalid signature");
        when(paymentService.handleVnpayReturn(anyMap())).thenReturn(response);

        mockMvc.perform(get("/api/payments/vnpay/return")
                        .param("vnp_TxnRef", UUID.randomUUID().toString())
                        .param("vnp_SecureHash", "invalid"))
                .andExpect(status().isOk());
    }

    @Test
    void vnpayIpn_success_noAuthorizationDoesNotReturn401_TC005() throws Exception {
        when(paymentService.handleVnpayIpn(anyMap()))
                .thenReturn(new VnpayIpnResponseDTO("97", "Invalid signature"));

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", UUID.randomUUID().toString())
                        .param("vnp_SecureHash", "invalid"))
                .andExpect(status().isOk());
    }

    @Test
    void stripeWebhook_fail_invalidSignatureIsRejectedByServiceNotAuth_TC006() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature"))
                .when(paymentService).handleStripeWebhook(anyString(), isNull());

        mockMvc.perform(post("/api/payments/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserEmail("luan123@example.com");
        return user;
    }
}
