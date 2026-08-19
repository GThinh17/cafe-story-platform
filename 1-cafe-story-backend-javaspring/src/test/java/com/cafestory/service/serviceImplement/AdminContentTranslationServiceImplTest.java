package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminContentTranslationRequestDTO;
import com.cafestory.dto.requestDTO.AdminContentTranslationWebhookRequestDTO;
import com.cafestory.dto.responseDTO.AdminContentTranslationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class AdminContentTranslationServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AdminReportAiWebhookSigner signer;

    private StubTranslationService service;

    @BeforeEach
    void setUp() {
        service = new StubTranslationService(objectMapper, signer);
    }

    @Test
    void translate_validProviderResponse_mapsExactContractWithoutPersistence() {
        service.responder = request -> responseFor(request, "en", "Xin chào URL https://cafe.test");
        doNothing().when(signer).verifyResponse(any(), any(), any(), any());

        AdminContentTranslationResponseDTO response = service.translate(request("Hello URL https://cafe.test"));

        assertThat(response.getDetectedLocale()).isEqualTo("en");
        assertThat(response.getTargetLocale()).isEqualTo("vi");
        assertThat(response.getTranslatedText()).contains("https://cafe.test");
        assertThat(service.captured.getContractVersion())
                .isEqualTo(AdminContentTranslationServiceImpl.CONTRACT_VERSION);
        assertThat(service.captured.getContentKind()).isEqualTo("COMMENT_CONTENT");
        verify(signer).verifyResponse(
                any(),
                any(),
                eq(AdminContentTranslationServiceImpl.CONTRACT_VERSION),
                eq(response.getRequestId()));
    }

    @Test
    void translate_invalidInput_rejectsBeforeProviderCall() {
        assertBadRequest(null);
        assertBadRequest(requestWith(null, "vi", "COMMENT_CONTENT"));
        assertBadRequest(requestWith(" ", "vi", "COMMENT_CONTENT"));
        assertBadRequest(requestWith("x".repeat(20_001), "vi", "COMMENT_CONTENT"));
        assertBadRequest(requestWith("Text", "fr", "COMMENT_CONTENT"));
        assertBadRequest(requestWith("Text", "vi", "UNKNOWN"));
        assertThat(service.captured).isNull();
    }

    @Test
    void translate_missingOrUnsignedProviderResponse_returnsSafeBadGateway() {
        service.responder = ignored -> null;
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = ignored -> new AdminContentTranslationServiceImpl.RawWebhookResponse(" ", new HttpHeaders());
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> responseFor(request, "en", "Xin chào");
        doThrow(new SecurityException("signature details must not escape"))
                .when(signer).verifyResponse(any(), any(), any(), any());
        assertProviderError(() -> service.translate(request("Hello")));
    }

    @Test
    void translate_malformedOrMismatchedProviderResponse_returnsSafeBadGateway() {
        doNothing().when(signer).verifyResponse(any(), any(), any(), any());
        service.responder = request -> raw("not-json");
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw("[]");
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(request.getRequestId(), "en", "vi", "Xin chào", "TRANSLATED", "gpt", ",\"extra\":true"));
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(UUID.randomUUID(), "en", "vi", "Xin chào", "TRANSLATED", "gpt", ""));
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(request.getRequestId(), "xx", "vi", "Xin chào", "TRANSLATED", "gpt", ""));
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(request.getRequestId(), "en", "en", "Xin chào", "TRANSLATED", "gpt", ""));
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(request.getRequestId(), "en", "vi", " ", "TRANSLATED", "gpt", ""));
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(request.getRequestId(), "en", "vi", "Xin chào", "FAILED", "gpt", ""));
        assertProviderError(() -> service.translate(request("Hello")));

        service.responder = request -> raw(json(request.getRequestId(), "en", "vi", "Xin chào", "TRANSLATED", " ", ""));
        assertProviderError(() -> service.translate(request("Hello")));
    }

    @Test
    void callWebhook_signedSuccess_forwardsHeadersAndBody() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://translation.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        AdminContentTranslationServiceImpl actual =
                new AdminContentTranslationServiceImpl(objectMapper, signer, client);
        UUID requestId = UUID.randomUUID();
        AdminContentTranslationWebhookRequestDTO request = webhookRequest(requestId);
        when(signer.signRequest(request, AdminContentTranslationServiceImpl.CONTRACT_VERSION, requestId))
                .thenReturn(new AdminReportAiWebhookSigner.SignedRequest(
                        "{\"signed\":true}",
                        AdminContentTranslationServiceImpl.CONTRACT_VERSION,
                        requestId.toString(),
                        "1700000000",
                        "nonce",
                        "a".repeat(64),
                        "b".repeat(64)));
        server.expect(once(), requestTo("http://translation.test"))
                .andExpect(header(AdminReportAiWebhookSigner.CONTRACT_VERSION_HEADER,
                        AdminContentTranslationServiceImpl.CONTRACT_VERSION))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON)
                        .header(AdminReportAiWebhookSigner.SIGNATURE_HEADER, "c".repeat(64)));

        AdminContentTranslationServiceImpl.RawWebhookResponse response = actual.callWebhook(request);

        assertThat(response.body()).isEqualTo("{}");
        assertThat(response.headers().getFirst(AdminReportAiWebhookSigner.SIGNATURE_HEADER))
                .isEqualTo("c".repeat(64));
        server.verify();
    }

    @Test
    void callWebhook_providerFailure_mapsToSafeBadGateway() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://translation.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        AdminContentTranslationServiceImpl actual =
                new AdminContentTranslationServiceImpl(objectMapper, signer, client);
        UUID requestId = UUID.randomUUID();
        AdminContentTranslationWebhookRequestDTO request = webhookRequest(requestId);
        when(signer.signRequest(any(), any(), any())).thenReturn(
                new AdminReportAiWebhookSigner.SignedRequest(
                        "{}",
                        AdminContentTranslationServiceImpl.CONTRACT_VERSION,
                        requestId.toString(),
                        "1700000000",
                        "nonce",
                        "a".repeat(64),
                        "b".repeat(64)));
        server.expect(once(), requestTo("http://translation.test")).andRespond(withServerError());

        assertProviderError(() -> actual.callWebhook(request));
        server.verify();
    }

    @Test
    void publicConstructor_acceptsMinimumTimeout() {
        AdminContentTranslationServiceImpl constructed =
                new AdminContentTranslationServiceImpl(
                        objectMapper,
                        signer,
                        "http://translation.test",
                        1);
        assertThat(constructed).isNotNull();
    }

    private void assertBadRequest(AdminContentTranslationRequestDTO invalid) {
        assertThatThrownBy(() -> service.translate(invalid))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getReason()).isEqualTo("Invalid translation request");
                });
    }

    private void assertProviderError(ThrowingAction action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(exception.getReason()).isEqualTo("Translation provider is unavailable");
                    assertThat(exception.getReason()).doesNotContain("signature details");
                });
    }

    private AdminContentTranslationRequestDTO request(String text) {
        return requestWith(text, "vi", "COMMENT_CONTENT");
    }

    private AdminContentTranslationRequestDTO requestWith(String text, String locale, String kind) {
        AdminContentTranslationRequestDTO request = new AdminContentTranslationRequestDTO();
        request.setText(text);
        request.setTargetLocale(locale);
        request.setContentKind(kind);
        return request;
    }

    private AdminContentTranslationWebhookRequestDTO webhookRequest(UUID requestId) {
        return AdminContentTranslationWebhookRequestDTO.builder()
                .contractVersion(AdminContentTranslationServiceImpl.CONTRACT_VERSION)
                .requestId(requestId)
                .text("Hello")
                .targetLocale("vi")
                .contentKind("COMMENT_CONTENT")
                .build();
    }

    private AdminContentTranslationServiceImpl.RawWebhookResponse responseFor(
            AdminContentTranslationWebhookRequestDTO request,
            String detectedLocale,
            String translatedText) {
        return raw(json(
                request.getRequestId(),
                detectedLocale,
                request.getTargetLocale(),
                translatedText,
                "TRANSLATED",
                "gpt-4o-mini",
                ""));
    }

    private AdminContentTranslationServiceImpl.RawWebhookResponse raw(String body) {
        return new AdminContentTranslationServiceImpl.RawWebhookResponse(body, new HttpHeaders());
    }

    private String json(
            UUID requestId,
            String detected,
            String target,
            String translated,
            String state,
            String model,
            String extra) {
        return "{"
                + "\"requestId\":\"" + requestId + "\","
                + "\"detectedLocale\":\"" + detected + "\","
                + "\"targetLocale\":\"" + target + "\","
                + "\"translatedText\":\"" + translated + "\","
                + "\"translationState\":\"" + state + "\","
                + "\"modelName\":\"" + model + "\""
                + extra
                + "}";
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run();
    }

    private static class StubTranslationService extends AdminContentTranslationServiceImpl {
        private AdminContentTranslationWebhookRequestDTO captured;
        private Function<AdminContentTranslationWebhookRequestDTO, RawWebhookResponse> responder;

        StubTranslationService(ObjectMapper objectMapper, AdminReportAiWebhookSigner signer) {
            super(objectMapper, signer, RestClient.create("http://unused.test"));
        }

        @Override
        protected RawWebhookResponse callWebhook(AdminContentTranslationWebhookRequestDTO request) {
            captured = request;
            return responder.apply(request);
        }
    }
}
