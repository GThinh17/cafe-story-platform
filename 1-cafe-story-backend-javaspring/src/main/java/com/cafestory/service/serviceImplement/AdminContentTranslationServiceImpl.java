package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminContentTranslationRequestDTO;
import com.cafestory.dto.requestDTO.AdminContentTranslationWebhookRequestDTO;
import com.cafestory.dto.responseDTO.AdminContentTranslationResponseDTO;
import com.cafestory.service.serviceInterface.AdminContentTranslationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminContentTranslationServiceImpl implements AdminContentTranslationService {

    static final String CONTRACT_VERSION = "ADMIN-CONTENT-TRANSLATION-V1";
    private static final Set<String> RESPONSE_KEYS = Set.of(
            "requestId",
            "detectedLocale",
            "targetLocale",
            "translatedText",
            "translationState",
            "modelName");
    private static final Set<String> DETECTED_LOCALES = Set.of("en", "vi", "und");
    private static final Logger log = LoggerFactory.getLogger(AdminContentTranslationServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AdminReportAiWebhookSigner webhookSigner;

    @Autowired
    public AdminContentTranslationServiceImpl(
            ObjectMapper objectMapper,
            AdminReportAiWebhookSigner webhookSigner,
            @Value("${admin.translation.webhook-url:http://localhost:5678/webhook/cafestory-admin-content-translation}")
            String webhookUrl,
            @Value("${admin.translation.timeout-ms:40000}") int timeoutMs) {
        this(
                objectMapper,
                webhookSigner,
                RestClient.builder()
                        .baseUrl(webhookUrl)
                        .requestFactory(requestFactory(timeoutMs))
                        .build());
    }

    AdminContentTranslationServiceImpl(
            ObjectMapper objectMapper,
            AdminReportAiWebhookSigner webhookSigner,
            RestClient restClient) {
        this.objectMapper = objectMapper;
        this.webhookSigner = webhookSigner;
        this.restClient = restClient;
    }

    @Override
    public AdminContentTranslationResponseDTO translate(AdminContentTranslationRequestDTO request) {
        validateInput(request);
        UUID requestId = UUID.randomUUID();
        AdminContentTranslationWebhookRequestDTO webhookRequest =
                AdminContentTranslationWebhookRequestDTO.builder()
                        .contractVersion(CONTRACT_VERSION)
                        .requestId(requestId)
                        .text(request.getText())
                        .targetLocale(request.getTargetLocale())
                        .contentKind(request.getContentKind())
                        .build();

        RawWebhookResponse rawResponse = callWebhook(webhookRequest);
        verifySignedResponse(rawResponse, requestId);
        return parseAndValidateResponse(rawResponse.body(), requestId, request.getTargetLocale());
    }

    protected RawWebhookResponse callWebhook(AdminContentTranslationWebhookRequestDTO request) {
        try {
            AdminReportAiWebhookSigner.SignedRequest signedRequest =
                    webhookSigner.signRequest(request, CONTRACT_VERSION, request.getRequestId());
            return restClient.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(signedRequest::apply)
                    .body(signedRequest.body())
                    .exchange((clientRequest, clientResponse) -> {
                        byte[] responseBytes = clientResponse.getBody().readAllBytes();
                        if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                            throw new IllegalStateException(
                                    "Translation provider returned " + clientResponse.getStatusCode());
                        }
                        HttpHeaders responseHeaders = new HttpHeaders();
                        responseHeaders.putAll(clientResponse.getHeaders());
                        return new RawWebhookResponse(
                                new String(responseBytes, StandardCharsets.UTF_8),
                                responseHeaders);
                    });
        } catch (RuntimeException exception) {
            log.warn(
                    "Admin content translation provider failed requestId={} reason={}",
                    request.getRequestId(),
                    exception.getClass().getSimpleName());
            throw providerError();
        }
    }

    private void verifySignedResponse(RawWebhookResponse response, UUID requestId) {
        if (response == null || response.body() == null || response.body().isBlank()) {
            throw providerError();
        }
        try {
            webhookSigner.verifyResponse(
                    response.body(),
                    response.headers(),
                    CONTRACT_VERSION,
                    requestId);
        } catch (RuntimeException exception) {
            log.warn(
                    "Admin content translation response security failed requestId={} reason={}",
                    requestId,
                    exception.getClass().getSimpleName());
            throw providerError();
        }
    }

    private AdminContentTranslationResponseDTO parseAndValidateResponse(
            String responseBody,
            UUID expectedRequestId,
            String expectedTargetLocale) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (!root.isObject() || !RESPONSE_KEYS.equals(fieldNames(root))) {
                throw providerError();
            }
            AdminContentTranslationResponseDTO response =
                    objectMapper.treeToValue(root, AdminContentTranslationResponseDTO.class);
            if (!expectedRequestId.equals(response.getRequestId())
                    || !expectedTargetLocale.equals(response.getTargetLocale())
                    || !DETECTED_LOCALES.contains(response.getDetectedLocale())
                    || !"TRANSLATED".equals(response.getTranslationState())
                    || isBlank(response.getTranslatedText())
                    || isBlank(response.getModelName())) {
                throw providerError();
            }
            return response;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn(
                    "Admin content translation response validation failed requestId={} reason={}",
                    expectedRequestId,
                    exception.getClass().getSimpleName());
            throw providerError();
        }
    }

    private Set<String> fieldNames(JsonNode root) {
        Set<String> names = new HashSet<>();
        Iterator<String> iterator = root.fieldNames();
        iterator.forEachRemaining(names::add);
        return names;
    }

    private void validateInput(AdminContentTranslationRequestDTO request) {
        if (request == null
                || isBlank(request.getText())
                || request.getText().length() > 20_000
                || !AdminContentTranslationRequestDTO.ALLOWED_TARGET_LOCALES.contains(request.getTargetLocale())
                || !AdminContentTranslationRequestDTO.ALLOWED_CONTENT_KINDS.contains(request.getContentKind())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid translation request");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException providerError() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Translation provider is unavailable");
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        int safeTimeoutMs = Math.max(1_000, timeoutMs);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(safeTimeoutMs);
        requestFactory.setReadTimeout(safeTimeoutMs);
        return requestFactory;
    }

    protected record RawWebhookResponse(String body, HttpHeaders headers) {
    }
}
