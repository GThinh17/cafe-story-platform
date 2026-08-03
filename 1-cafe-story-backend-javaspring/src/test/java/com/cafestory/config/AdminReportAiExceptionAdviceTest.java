package com.cafestory.config;

import com.cafestory.exception.AdminReportAiProviderBoundaryException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminReportAiExceptionAdviceTest {

    @Test
    void providerBoundaryFailure_returnsStructuredRetryableContract_TC001() throws Exception {
        UUID correlationId = UUID.randomUUID();
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new FailingController(correlationId))
                .setControllerAdvice(
                        new AdminReportAiExceptionAdvice(),
                        new GlobalResponseAdvice(new ObjectMapper()))
                .build();

        mockMvc.perform(get("/test/admin-report-ai/provider-boundary"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(502))
                .andExpect(jsonPath("$.status").value("Fail"))
                .andExpect(jsonPath("$.message")
                        .value(AdminReportAiProviderBoundaryException.SAFE_MESSAGE))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.code")
                        .value(AdminReportAiProviderBoundaryException.CODE))
                .andExpect(jsonPath("$.correlationId").value(correlationId.toString()))
                .andExpect(jsonPath("$.retryable").value(true))
                .andExpect(jsonPath("$.stage")
                        .value(AdminReportAiProviderBoundaryException.STAGE));
    }

    @RestController
    private static class FailingController {

        private final UUID correlationId;

        private FailingController(UUID correlationId) {
            this.correlationId = correlationId;
        }

        @GetMapping("/test/admin-report-ai/provider-boundary")
        void fail() {
            throw new AdminReportAiProviderBoundaryException(
                    correlationId,
                    new IllegalStateException("synthetic provider outage"));
        }
    }
}
