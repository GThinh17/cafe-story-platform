package com.cafestory.integration;

import com.cafestory.entity.Payment;
import com.cafestory.entity.PaymentDetail;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PaymentMethod;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.repository.PaymentDetailRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.VnpayPaymentClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentVnpayPostgresIntegrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Autowired
    private VnpayPaymentClient vnpayPaymentClient;

    @Test
    void vnpayIpn_success_validSignatureUpdatesPaymentAndReturnsUnwrappedResponse() throws Exception {
        Payment payment = pendingVnpayPayment("vnpaybuyer01", new BigDecimal("10000.00"));
        Map<String, String> params = successfulIpnParams(payment);

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", params.get("vnp_TxnRef"))
                        .param("vnp_Amount", params.get("vnp_Amount"))
                        .param("vnp_ResponseCode", params.get("vnp_ResponseCode"))
                        .param("vnp_TransactionStatus", params.get("vnp_TransactionStatus"))
                        .param("vnp_TransactionNo", params.get("vnp_TransactionNo"))
                        .param("vnp_SecureHash", params.get("vnp_SecureHash")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"))
                .andExpect(jsonPath("$.data").doesNotExist());

        Payment updated = paymentRepository.findById(payment.getPaymentId()).orElseThrow();
        assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updated.getPaidAt()).isNotNull();
        PaymentDetail detail = paymentDetailRepository.findByPaymentPaymentId(payment.getPaymentId()).orElseThrow();
        assertThat(detail.getProviderTransactionId()).isEqualTo("14123456");
        assertThat(detail.getFailureCode()).isNull();
    }

    @Test
    void vnpayIpn_fail_invalidSignatureDoesNotUpdatePayment() throws Exception {
        Payment payment = pendingVnpayPayment("vnpaybuyer02", new BigDecimal("10000.00"));

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", payment.getPaymentId().toString())
                        .param("vnp_Amount", "1000000")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionStatus", "00")
                        .param("vnp_TransactionNo", "14123456")
                        .param("vnp_SecureHash", "invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97"))
                .andExpect(jsonPath("$.data").doesNotExist());

        Payment unchanged = paymentRepository.findById(payment.getPaymentId()).orElseThrow();
        assertThat(unchanged.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(unchanged.getPaidAt()).isNull();
    }

    private Payment pendingVnpayPayment(String username, BigDecimal amount) {
        User buyer = new User();
        buyer.setUserName(username);
        buyer.setUserFullName("VNPAY Buyer");
        buyer.setUserEmail(username + "@example.com");
        buyer.setUserPassword("encoded-password");
        buyer.setAccountStatus(true);
        buyer = userRepository.save(buyer);

        Payment payment = new Payment();
        payment.setBuyer(buyer);
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        payment.setAmount(amount);
        payment.setCurrency("VND");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        payment = paymentRepository.save(payment);

        PaymentDetail detail = new PaymentDetail();
        detail.setPayment(payment);
        detail.setProviderName("VNPAY");
        paymentDetailRepository.save(detail);

        return payment;
    }

    private Map<String, String> successfulIpnParams(Payment payment) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_TxnRef", payment.getPaymentId().toString());
        params.put("vnp_Amount", "1000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "14123456");
        params.put("vnp_SecureHash", vnpayPaymentClient.secureHash(params));
        return params;
    }
}
