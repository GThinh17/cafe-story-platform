package com.cafestory.config;

import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.dto.responseDTO.VnpayReturnResponseDTO;
import com.cafestory.dto.responseDTO.UsernameSuggestionResponse;
import com.cafestory.dto.responseDTO.reviewer.ReviewerBadgeResponseDTO;
import com.cafestory.dto.responseDTO.reviewer.ReviewerPayoutResponseDTO;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.service.serviceInterface.AuthService;
import com.cafestory.service.serviceInterface.PaymentService;
import com.cafestory.service.serviceInterface.ReviewerService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @MockBean
    private AuthService authService;

    @MockBean
    private ReviewerService reviewerService;

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
    void swaggerDocs_success_noAuthorizationReturnsOpenApiDocument_TC008() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists());
    }

    @Test
    void adminUsers_fail_bearerUserRoleCannotAccess_TC009() throws Exception {
        String accessToken = jwtService.createAccessToken(user(), List.of("USER"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + accessToken))
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
    void usernameSuggestions_success_noAuthorizationDoesNotReturn401_TC008() throws Exception {
        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setSuggestions(List.of("thanh.vu", "thanhvu_7"));
        when(authService.suggestUserNames("Phạm Thanh Vũ")).thenReturn(response);

        mockMvc.perform(get("/api/auth/usernames/suggestions")
                        .param("fullName", "Phạm Thanh Vũ"))
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

    @Test
    void reviewerPayoutHistory_success_reviewerRoleCanReachSelfOrAdminService_TC010() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        String accessToken = jwtService.createAccessToken(user(userId), List.of("REVIEWER"));
        when(reviewerService.getReviewerPayoutHistory(userId, reviewerId)).thenReturn(List.of(new ReviewerPayoutResponseDTO()));

        mockMvc.perform(get("/api/reviewers/{reviewerId}/payouts", reviewerId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void reviewerBadgeHistory_success_reviewerRoleCanReachSelfOrAdminService_TC011() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        String accessToken = jwtService.createAccessToken(user(userId), List.of("REVIEWER"));
        when(reviewerService.getReviewerBadgeHistory(userId, reviewerId)).thenReturn(List.of(new ReviewerBadgeResponseDTO()));

        mockMvc.perform(get("/api/reviewers/{reviewerId}/badges", reviewerId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void reviewerPayoutGenerate_fail_reviewerRoleCannotAccessAdminGeneration_TC012() throws Exception {
        String accessToken = jwtService.createAccessToken(user(), List.of("REVIEWER"));

        mockMvc.perform(post("/api/reviewers/payouts/generate")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("month", "2026-06"))
                .andExpect(status().isForbidden());
    }

    private User user() {
        return user(UUID.randomUUID());
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("luan123");
        user.setUserEmail("luan123@example.com");
        return user;
    }
}
