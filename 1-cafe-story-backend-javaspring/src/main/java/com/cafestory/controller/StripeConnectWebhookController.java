package com.cafestory.controller;

import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerConnectService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Transfer;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/stripe-connect")
public class StripeConnectWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeConnectWebhookController.class);

    private final String connectWebhookSecret;
    private final ReviewerConnectService connectService;
    private final AdminPayoutService payoutService;

    public StripeConnectWebhookController(
            @Value("${stripe.connect-webhook-secret:}") String connectWebhookSecret,
            ReviewerConnectService connectService,
            AdminPayoutService payoutService) {
        this.connectWebhookSecret = connectWebhookSecret;
        this.connectService = connectService;
        this.payoutService = payoutService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, connectWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe Connect webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "account.updated" -> deserialize(event, Account.class).ifPresent(account ->
                    connectService.handleAccountUpdated(
                            account.getId(),
                            Boolean.TRUE.equals(account.getChargesEnabled()),
                            Boolean.TRUE.equals(account.getPayoutsEnabled())));
            // Transfer không mang trường lý do trong SDK này; event id là đầu mối
            // duy nhất để tra lại trên Stripe dashboard nên phải ghi vào note.
            case "transfer.failed" -> deserialize(event, Transfer.class).ifPresent(transfer ->
                    payoutService.markTransferFailed(transfer.getId(), "event " + event.getId()));
            case "account.application.deauthorized" ->
                log.warn("Reviewer deauthorized Stripe Connect — event id: {}", event.getId());
            default ->
                log.debug("Unhandled Stripe Connect event: {}", event.getType());
        }

        return ResponseEntity.ok("ok");
    }

    /**
     * Bóc payload của event thành object Stripe.
     *
     * <p>{@code getObject()} trả rỗng khi API version của event lệch với version
     * SDK đang dùng. Trước đây chỗ này là {@code ifPresent} trần: event bị bỏ
     * qua hoàn toàn trong im lặng, hậu quả là payouts_enabled không bao giờ được
     * bật và mọi transfer sau đó đều báo "not fully verified" mà không rõ vì
     * sao. Nay lệch version phải để lại dấu vết.
     */
    private <T> Optional<T> deserialize(Event event, Class<T> type) {
        Optional<StripeObject> raw = event.getDataObjectDeserializer().getObject();
        if (raw.isEmpty()) {
            log.error(
                    "Không deserialize được payload Stripe: type={}, eventId={}, eventApiVersion={}."
                            + " Nhiều khả năng API version của webhook lệch với stripe-java SDK.",
                    event.getType(), event.getId(), event.getApiVersion());
            return Optional.empty();
        }
        StripeObject object = raw.get();
        if (!type.isInstance(object)) {
            log.error("Payload Stripe không đúng kiểu mong đợi: eventId={}, mong {} nhận {}",
                    event.getId(), type.getSimpleName(), object.getClass().getSimpleName());
            return Optional.empty();
        }
        return Optional.of(type.cast(object));
    }
}
