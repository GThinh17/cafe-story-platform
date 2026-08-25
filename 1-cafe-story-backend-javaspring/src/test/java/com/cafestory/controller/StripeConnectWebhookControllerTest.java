package com.cafestory.controller;

import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerConnectService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Transfer;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link StripeConnectWebhookController}.
 *
 * <p>{@code Webhook.constructEvent} là phương thức tĩnh của SDK nên được chặn
 * bằng {@code mockStatic}: bài kiểm thử tự quyết định event mà Stripe "gửi tới",
 * kể cả trường hợp payload không bóc tách được vì lệch API version — nhánh từng
 * làm cờ payouts_enabled không bao giờ bật.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StripeConnectWebhookControllerTest {

    private static final String PAYLOAD = "{\"id\":\"evt_1\"}";
    private static final String SIGNATURE = "t=1,v1=abc";

    @Mock
    private ReviewerConnectService connectService;
    @Mock
    private AdminPayoutService payoutService;

    private StripeConnectWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new StripeConnectWebhookController("whsec_test", connectService, payoutService);
    }

    @Test
    void handleWebhook_fail_invalidSignature_TC001() throws Exception {
        try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
            webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenThrow(new SignatureVerificationException("bad signature", SIGNATURE));

            ResponseEntity<String> response = controller.handleWebhook(PAYLOAD, SIGNATURE);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isEqualTo("Invalid signature");
        }
        verifyNoInteractions(connectService, payoutService);
    }

    @Test
    void handleWebhook_success_accountUpdatedSyncsConnectFlags_TC002() {
        Account account = new Account();
        account.setId("acct_123");
        account.setChargesEnabled(true);
        account.setPayoutsEnabled(true);
        Event event = event("account.updated", account);

        assertThat(handle(event).getBody()).isEqualTo("ok");

        verify(connectService).handleAccountUpdated("acct_123", true, true);
    }

    @Test
    void handleWebhook_success_accountUpdatedWithNullFlags_TC003() {
        Account account = new Account();
        account.setId("acct_123");
        Event event = event("account.updated", account);

        handle(event);

        verify(connectService).handleAccountUpdated("acct_123", false, false);
    }

    @Test
    void handleWebhook_success_transferFailedRollsBackPayout_TC004() {
        Transfer transfer = new Transfer();
        transfer.setId("tr_123");
        Event event = event("transfer.failed", transfer);

        handle(event);

        verify(payoutService).markTransferFailed("tr_123", "event evt_1");
    }

    @Test
    void handleWebhook_success_deauthorizedEventIsOnlyLogged_TC005() {
        Event event = event("account.application.deauthorized", null);

        assertThat(handle(event).getBody()).isEqualTo("ok");

        verifyNoInteractions(connectService, payoutService);
    }

    @Test
    void handleWebhook_success_unknownEventTypeIsIgnored_TC006() {
        Event event = event("payout.paid", null);

        assertThat(handle(event).getBody()).isEqualTo("ok");

        verifyNoInteractions(connectService, payoutService);
    }

    @Test
    void handleWebhook_success_undeserializablePayloadIsSkipped_TC007() {
        // getObject() rỗng = API version của webhook lệch với SDK.
        Event event = event("account.updated", null);

        assertThat(handle(event).getBody()).isEqualTo("ok");

        verify(connectService, never()).handleAccountUpdated(anyString(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void handleWebhook_success_payloadOfWrongTypeIsSkipped_TC008() {
        // Event khai là account.updated nhưng payload lại là Transfer.
        Transfer transfer = new Transfer();
        transfer.setId("tr_123");
        Event event = event("account.updated", transfer);

        assertThat(handle(event).getBody()).isEqualTo("ok");

        verify(connectService, never()).handleAccountUpdated(anyString(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    private ResponseEntity<String> handle(Event event) {
        try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
            webhook.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(event);
            return controller.handleWebhook(PAYLOAD, SIGNATURE);
        }
    }

    private Event event(String type, StripeObject payload) {
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.ofNullable(payload));
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);
        when(event.getId()).thenReturn("evt_1");
        when(event.getApiVersion()).thenReturn("2024-06-20");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        return event;
    }
}
