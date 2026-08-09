package com.cafestory.service.serviceImplement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

class AdminReportAiWebhookSignerTest {

    private static final Instant BASE_TIME = Instant.parse("2026-07-26T05:00:00Z");
    private static final String SECRET = "test-only-report-ai-hmac-secret";

    private ObjectMapper objectMapper;
    private MutableClock clock;
    private AdminReportAiWebhookSigner signer;
    private UUID correlationId;
    private Map<String, Object> payload;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        clock = new MutableClock(BASE_TIME);
        signer = new AdminReportAiWebhookSigner(objectMapper, SECRET, 120, 300, clock);
        correlationId = UUID.randomUUID();
        payload = new LinkedHashMap<>();
        payload.put("z", List.of(Map.of("b", 2, "a", 1)));
        payload.put("a", "value");
    }

    @Test
    void signRequest_usesCanonicalBodyAndCompleteSecurityHeaders_TC001() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);
        HttpHeaders headers = new HttpHeaders();

        signed.apply(headers);

        assertThat(signed.body()).isEqualTo("{\"a\":\"value\",\"z\":[{\"a\":1,\"b\":2}]}");
        assertThat(signed.timestamp()).isEqualTo(String.valueOf(BASE_TIME.getEpochSecond()));
        assertThat(signed.bodyHash()).matches("[0-9a-f]{64}");
        assertThat(signed.signature()).matches("[0-9a-f]{64}");
        assertThat(headers.getFirst(AdminReportAiWebhookSigner.CONTRACT_VERSION_HEADER)).isEqualTo("2.0");
        assertThat(headers.getFirst(AdminReportAiWebhookSigner.CORRELATION_ID_HEADER))
                .isEqualTo(correlationId.toString());
        assertThat(headers.getFirst(AdminReportAiWebhookSigner.NONCE_HEADER)).isEqualTo(signed.nonce());
    }

    @Test
    void canonicalJson_usesJcsEcmaNumberSerialization_TC001_1() {
        assertThat(signer.canonicalJson(Map.of(
                "wholeDouble", 1.0d,
                "fraction", 0.000001d,
                "large", 1.0e30d)))
                .isEqualTo("{\"fraction\":0.000001,\"large\":1e+30,\"wholeDouble\":1}");
    }

    @Test
    void verifyResponse_acceptsValidSignedCanonicalPayload_TC002() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);

        signer.verifyResponse(signed.body(), headers(signed), "2.0", correlationId);
    }

    @Test
    void verifyResponse_rejectsMissingHeaders_TC003() {
        assertThatThrownBy(() -> signer.verifyResponse("{}", new HttpHeaders(), "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("headers are missing");
    }

    @Test
    void verifyResponse_rejectsContractOrCorrelationMismatch_TC004() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);

        assertThatThrownBy(() -> signer.verifyResponse(
                signed.body(), headers(signed), "2.1", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("contract or correlation mismatch");
        assertThatThrownBy(() -> signer.verifyResponse(
                signed.body(), headers(signed), "2.0", UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("contract or correlation mismatch");
    }

    @Test
    void verifyResponse_rejectsStaleOrFutureTimestamp_TC005() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);
        HttpHeaders stale = headersWithTimestamp(signed, BASE_TIME.minusSeconds(121).getEpochSecond());
        HttpHeaders future = headersWithTimestamp(signed, BASE_TIME.plusSeconds(121).getEpochSecond());

        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), stale, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("outside the allowed window");
        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), future, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("outside the allowed window");
    }

    @Test
    void verifyResponse_rejectsNonNumericTimestamp_TC006() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);
        HttpHeaders headers = headers(signed);
        headers.set(AdminReportAiWebhookSigner.TIMESTAMP_HEADER, "not-a-number");

        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), headers, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("timestamp is invalid");
    }

    @Test
    void verifyResponse_rejectsInvalidNonceOrDigestFormat_TC007() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);
        HttpHeaders invalidNonce = headers(signed);
        invalidNonce.set(AdminReportAiWebhookSigner.NONCE_HEADER, "bad nonce!");
        HttpHeaders invalidDigest = headers(signed);
        invalidDigest.set(AdminReportAiWebhookSigner.BODY_SHA256_HEADER, "not-a-sha256");

        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), invalidNonce, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("nonce is invalid");
        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), invalidDigest, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("digest format is invalid");
    }

    @Test
    void verifyResponse_rejectsBodyTamperAndInvalidSignature_TC008() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);
        HttpHeaders invalidSignature = headers(signed);
        invalidSignature.set(AdminReportAiWebhookSigner.SIGNATURE_HEADER, "0".repeat(64));

        assertThatThrownBy(() -> signer.verifyResponse(
                "{\"a\":\"tampered\"}", headers(signed), "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("body hash is invalid");
        assertThatThrownBy(() -> signer.verifyResponse(
                signed.body(), invalidSignature, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("signature is invalid");
    }

    @Test
    void verifyResponse_rejectsAuthenticatedNonceReplay_TC009() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);
        HttpHeaders headers = headers(signed);
        signer.verifyResponse(signed.body(), headers, "2.0", correlationId);

        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), headers, "2.0", correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("nonce was replayed");
    }

    @Test
    void verifyResponse_allowsNonceReuseOnlyAfterTtlExpiry_TC010() {
        signer = new AdminReportAiWebhookSigner(objectMapper, SECRET, 500, 300, clock);
        AdminReportAiWebhookSigner.SignedRequest first =
                signer.signRequest(payload, "2.0", correlationId);
        signer.verifyResponse(first.body(), headers(first), "2.0", correlationId);
        clock.advanceSeconds(301);
        String timestamp = String.valueOf(clock.instant().getEpochSecond());
        String signature = signer.signature(timestamp, first.nonce(), first.bodyHash());
        HttpHeaders afterExpiry = headers(first);
        afterExpiry.set(AdminReportAiWebhookSigner.TIMESTAMP_HEADER, timestamp);
        afterExpiry.set(AdminReportAiWebhookSigner.SIGNATURE_HEADER, signature);

        signer.verifyResponse(first.body(), afterExpiry, "2.0", correlationId);
    }

    @Test
    void signer_failsClosedForMissingSecretOrMetadata_TC011() {
        AdminReportAiWebhookSigner missingSecret =
                new AdminReportAiWebhookSigner(objectMapper, " ", 120, 300, clock);

        assertThatThrownBy(() -> missingSecret.signRequest(payload, "2.0", correlationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("secret is not configured");
        assertThatThrownBy(() -> signer.signRequest(payload, " ", correlationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("metadata is required");
        assertThatThrownBy(() -> signer.signRequest(payload, "2.0", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("metadata is required");
    }

    @Test
    void signer_rejectsInvalidConfigurationOrJson_TC012() {
        assertThat(new AdminReportAiWebhookSigner(objectMapper, SECRET, 120, 300))
                .isNotNull();
        assertThatThrownBy(() -> new AdminReportAiWebhookSigner(objectMapper, SECRET, 0, 300, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("windows must be positive");
        assertThatThrownBy(() -> new AdminReportAiWebhookSigner(objectMapper, SECRET, 120, -1, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("windows must be positive");
        assertThatThrownBy(() -> signer.canonicalJson("{not-json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid JSON");
        assertThatThrownBy(() -> signer.canonicalJson(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid JSON");
    }

    @Test
    void verifyResponse_rejectsMissingExpectedCorrelationMetadata_TC013() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);

        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), headers(signed), "2.0", null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("security metadata is missing");
    }

    @Test
    void verifyResponse_rejectsMissingExpectedContractMetadata_TC014() {
        AdminReportAiWebhookSigner.SignedRequest signed =
                signer.signRequest(payload, "2.0", correlationId);

        assertThatThrownBy(() -> signer.verifyResponse(signed.body(), headers(signed), null, correlationId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("contract or correlation mismatch");
    }

    @Test
    void signer_wrapsUnavailableCryptoAlgorithms_TC015() {
        try (MockedStatic<Mac> mac = mockStatic(Mac.class)) {
            mac.when(() -> Mac.getInstance("HmacSHA256"))
                    .thenThrow(new NoSuchAlgorithmException("test"));

            assertThatThrownBy(() -> signer.signature("1", "nonce", "0".repeat(64)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Unable to sign");
        }

        try (MockedStatic<MessageDigest> digest = mockStatic(MessageDigest.class)) {
            digest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("test"));

            assertThatThrownBy(() -> signer.signRequest(payload, "2.0", correlationId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SHA-256 is not available");
        }
    }

    private HttpHeaders headers(AdminReportAiWebhookSigner.SignedRequest signed) {
        HttpHeaders headers = new HttpHeaders();
        signed.apply(headers);
        return headers;
    }

    private HttpHeaders headersWithTimestamp(
            AdminReportAiWebhookSigner.SignedRequest signed,
            long epochSeconds) {
        HttpHeaders headers = headers(signed);
        String timestamp = String.valueOf(epochSeconds);
        headers.set(AdminReportAiWebhookSigner.TIMESTAMP_HEADER, timestamp);
        headers.set(
                AdminReportAiWebhookSigner.SIGNATURE_HEADER,
                signer.signature(timestamp, signed.nonce(), signed.bodyHash()));
        return headers;
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }
    }
}
